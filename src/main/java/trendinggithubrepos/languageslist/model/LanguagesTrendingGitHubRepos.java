/*
 * Copyright (c) 2020.
 * Author : Abdellah Touili, Full Stack Developer Senior - IT Specialist. Email:atouilim@hotmail.com
 */

package trendinggithubrepos.languageslist.model;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Data;

//Use of lombok annotation to generate implicitly/automatically the constructor, getters, setters, ect.
@Data
@AllArgsConstructor
public class LanguagesTrendingGitHubRepos {
    String languageName;
    ArrayList<String> listReposByLanguage = new ArrayList<String>();
    Integer nbrReposByLanguage;
}
