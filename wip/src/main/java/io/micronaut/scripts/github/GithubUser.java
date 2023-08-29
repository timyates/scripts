package io.micronaut.scripts.github;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record GithubUser(String login) {
}
