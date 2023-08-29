package io.micronaut.scripts.github;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record GithubPullRequest(String url, GithubUser user) {
}
