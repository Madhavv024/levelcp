package com.levelUpZone.levelUpZone_backend.Util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.levelUpZone.levelUpZone_backend.Entity.CodeforcesProblemEntity;
import com.levelUpZone.levelUpZone_backend.DAO.CodeforcesProblemDAO;
import jakarta.transaction.Transactional;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;


@Service
public class CFUtility {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(CFUtility.class);
    static Logger logger = Logger.getLogger(CFUtility.class.getName());
    // all the cf calling methods will be here

    @Value(value = "${cfURL}")
    private static String cfUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final CodeforcesProblemDAO problemRepository;

    public CFUtility(CodeforcesProblemDAO problemRepository) {
        this.problemRepository = problemRepository;
    }


    public ResponseEntity<?> getUserResponse(String userHandle) {
        try {
            String url = cfUrl +"user.info?handles="+  userHandle;
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            return response;
        }catch (Exception e){
            logger.severe(e.getMessage());
            return null;
        }
    }

    @Transactional
    public void fetchAndUpdateProblems() {
        try {
            String url = "https://codeforces.com/api/problemset.problems";
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new RuntimeException("Failed to fetch problems from Codeforces");
            }

            JsonNode root = response.getBody().get("result");
            JsonNode problems = root.get("problems");
            JsonNode stats = root.get("problemStatistics");

            // Build a map of contestId+index -> solvedCount for faster lookup
            Map<String, Integer> solvedCountMap = new TreeMap<>();
            for (JsonNode stat : stats) {
                String key = stat.get("contestId").asText() + "-" + stat.get("index").asText();
                solvedCountMap.put(key, stat.get("solvedCount").asInt());
            }

            int cnt = 0;

            // Iterate problems and save to DB
            for (JsonNode problem : problems) {
                int contestId = problem.get("contestId").asInt();
                String index = problem.get("index").asText();
                String name = problem.get("name").asText();
                int rating = problem.has("rating") ? problem.get("rating").asInt() : 0;
                int points = problem.has("points") ? problem.get("points").asInt() : 0;

                // Convert tags array to comma-separated string
                StringBuilder tagsBuilder = new StringBuilder();
                if (problem.has("tags")) {
                    Iterator<JsonNode> it = problem.get("tags").elements();
                    while (it.hasNext()) {
                        tagsBuilder.append(it.next().asText());
                        if (it.hasNext()) tagsBuilder.append(",");
                    }
                }
                String tags = tagsBuilder.toString();

                String key = contestId + "-" + index;
                int solvedCount = solvedCountMap.getOrDefault(key, 0);

                // Upsert (save or update if exists)
                CodeforcesProblemEntity p = problemRepository.findByCfContestIdAndCfProblemId(contestId, index)
                        .orElse(new CodeforcesProblemEntity());

                // ____________ check the problem _____________
//                if(p.getProblemName().equalsIgnoreCase("BattleCows 2")){
//                    logger.info("BattleCows 2");
//                    logger.info(tags);
//                    logger.info("problem rating --- -- " + rating);
//                    break;
//                }

                p.setCfContestId(contestId);
                p.setCfProblemId(index);
                p.setProblemName(name);
                p.setProblemRating(rating != 0 ? rating : points);
                p.setProblemTags(tags);
                p.setCfProblemSolvedCount(solvedCount);
                p.setActive(true);
                if(p.getId()!=null){
                    p.setVersion(p.getVersion()+1);
                    p.setUpdatedAt(OffsetDateTime.now());
                }else{
                    p.setVersion(1);
                    p.setUpdatedAt(OffsetDateTime.now());
                }

                problemRepository.save(p);


            }

            logger.info("Codeforces problems successfully fetched and updated!");

        } catch (Exception e) {
            logger.severe("Failed to fetch problems from Codeforces: " + e.getMessage());
            throw new RuntimeException("Failed to fetch problems from Codeforces", e);
        }
    }


}
