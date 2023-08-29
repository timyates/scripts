package io.micronaut.scripts.github;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.client.annotation.Client;

import java.util.List;

import static io.micronaut.http.HttpHeaders.*;

@Client(GithubConfiguration.GITHUB_API_URL)
@Header(name = USER_AGENT, value = "Micronaut HTTP Client")
@Header(name = ACCEPT, value = "application/vnd.github.v3+json, application/json")
@Header(name = AUTHORIZATION, value = "token ${github.token}")
public interface GithubApiClient {

    @Get("/repos/{organization}/{repo}/pulls")
    List<GithubPullRequest> pulls(String organization, String repo);

    @Get("/repos/{organization}/{repo}/issues")
    HttpResponse<List<GithubIssue>> issues(String organization, String repo,
                                          @Nullable @QueryValue Integer page,
                                          @Nullable @QueryValue("per_page") Integer perPage);
}
