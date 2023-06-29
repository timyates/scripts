package io.micronaut.bomversions;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.client.annotation.Client;

import java.util.List;

import static io.micronaut.http.HttpHeaders.ACCEPT;
import static io.micronaut.http.HttpHeaders.AUTHORIZATION;
import static io.micronaut.http.HttpHeaders.USER_AGENT;

@Client(GithubConfiguration.GITHUB_API_URL)
@Header(name = USER_AGENT, value = "Micronaut HTTP Client")
@Header(name = ACCEPT, value = "application/vnd.github.v3+json, application/json")
@Header(name = AUTHORIZATION, value = "token ${github.token}")
public interface GithubApiClient {

    @Get("/repos/{organization}/{repo}/releases/latest")
    GithubRelease latest(String organization, String repo);

    @Get("/repos/{organization}/{repo}/releases")
    List<GithubRelease> releases(String organization, String repo, @Nullable @QueryValue("per_page") Integer perPage);
}
