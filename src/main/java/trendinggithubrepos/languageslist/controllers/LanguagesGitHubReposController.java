/*
 * Copyright (c) 2020.
 * Author : Abdellah Touili, Full Stack Developer Senior - IT Specialist. Email:atouilim@hotmail.com
 */

package trendinggithubrepos.languageslist.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import trendinggithubrepos.languageslist.model.LanguagesTrendingGitHubRepos;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api-repo")
public class LanguagesGitHubReposController {

    //To store the languages and the list of their corresponding Trending Repositories.
    HashMap<String, ArrayList<String>> reposByLanguageMap = new HashMap<String, ArrayList<String>>();
    //The Rest Api, Controller, return a list of 'LanguagesTrendingGitHubRepos' objects
    ArrayList<LanguagesTrendingGitHubRepos> listLanguagesTrendingGitHubRepos = new ArrayList<LanguagesTrendingGitHubRepos>();
    //Here, We get the Trending GitHub repositories by language programming
    String filterRepoBykeyWord = "language";

    @GetMapping("/languages-trending-gitHubRepo")
    public ArrayList<LanguagesTrendingGitHubRepos> LanguagesGitHubRepo() {
        //We get the Repos of the last 30 days
        LocalDate currentDate = LocalDate.now();
        Integer DaysNumber = 30;
        LocalDate LastReposDate = currentDate.minusDays(DaysNumber);

        //N.B: I used 'restTemplate' Spring REST Client to get response from the GitHub Repos Api. But When I finished this small project (Challenge). I read that for the new Spring 5 Framework.
        //'WebClient' must be used for the future to perform HTTP requests As 'restTemplate' will be deprecated.
        // Plz, take a look at this page: https://rieckpil.de/howto-use-spring-webclient-for-restful-communication/.
        // To use the WebClient api to get response as JsonNode

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JsonNode> response = null;
        //The Url Api to get the 100 first Trending GitHub repositories for the last 30 days (by default, this GitHub Api return 30 repos by page and it's pageable)
        String urlGitHubApi = "https://api.github.com/search/repositories?q=created:>" + LastReposDate + "&sort=stars&per_page=100&order=desc";

        //Handle any exception/error that will happen during the call to the Api
        try {
            response = restTemplate.exchange(urlGitHubApi, HttpMethod.GET, HttpEntity.EMPTY, JsonNode.class);
        } catch (HttpStatusCodeException e) {
            ResponseEntity.status(e.getRawStatusCode()).headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsString());
        } catch (RestClientException e) {
            ResponseEntity.status(HttpStatus.valueOf(e.getMessage())).header(e.getMessage()).body(e.getMessage());
        }

        //It's easier to get the response as JsonNode object to handle it rapidly.
        // The 'language name'(key = "language") and its GitHub Urls (key = "html_url") are stored in the 'items' array returned by the API (json File)
        JsonNode repoResults = response.getBody().findValue("items");

        //We test if the GitHub Repos API returns somme result
        if (repoResults != null) {
            fillHashMap(repoResults);
            reposByLanguageMap.forEach((key, value) ->
            {
                LanguagesTrendingGitHubRepos languagesTrendingGitHubRepos = new LanguagesTrendingGitHubRepos(key, value, value.size());
                listLanguagesTrendingGitHubRepos.add(languagesTrendingGitHubRepos);
            });

            //We sort our 'Language Trending GitHub Repos' list by the Number of the corresponding Trending GitHub Repos in the descending order
            Comparator<LanguagesTrendingGitHubRepos> compareByNbrRepository = (LanguagesTrendingGitHubRepos o1, LanguagesTrendingGitHubRepos o2) -> {
                return o1.getNbrReposByLanguage().compareTo(o2.getNbrReposByLanguage());
            };
            Collections.sort(listLanguagesTrendingGitHubRepos, compareByNbrRepository.reversed());

        } else {
            System.out.println("The response from the API Call is Null");
        }
        return listLanguagesTrendingGitHubRepos;
    }

    /**
     * @param root, This function parse the JsonNode, recuperate all the Languages Names (Keys of the HashMap) with their corresponding GitHubRepos URL(the values).
     *              And fill in the HashMap.
     *              N.B: The GitHubRepos URL without language (language == Null) are also included. Of course we can decide to account/take only the 100 GitHub Repos
     *              with a Real/Existing Language (Easy to code :-))
     */
    public void fillHashMap(JsonNode root) {
        if (root.isObject()) {
            //For each element/node of the JsonNode, we retrieve the 'language name' and built it's corresponding GitHub Url (list)
            Iterator<String> fieldNames = root.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldValue = root.get(fieldName);

                if (fieldName.equalsIgnoreCase(filterRepoBykeyWord)) {

                    String urlRepo = root.get("html_url").asText();
                    //If it exist, we recuperate the list of the URL Repo corresponding to the founded language
                    ArrayList urlRepoList = (ArrayList) reposByLanguageMap.get(fieldValue.asText());
                    //And if this list is Empty/Null, so we add the first Url Repo for a founded language name
                    if (urlRepoList == null) {
                        urlRepoList = new ArrayList<String>();
                        urlRepoList.add(urlRepo);
                        //And if a founded language name already exist in the 'reposByLanguageMap' HashMap, we add the corresponding Url Repo to the this list
                    } else urlRepoList.add(urlRepo);

                    //fill in the reposByLanguageMap (HashMap) which doesn't permit the Duplication of the Keys (language Name)
                    reposByLanguageMap.put(fieldValue.asText(), urlRepoList);

                }
            }
        }
        //We do the same process for All the elements/nodes of the JsonNode, for that we use it as an ArrayNode
        else if (root.isArray()) {
            ArrayNode arrayNode = (ArrayNode) root;
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode arrayElement = arrayNode.get(i);
                fillHashMap(arrayElement);
            }
        }

    }
}
