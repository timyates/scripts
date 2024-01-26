Usage:

Set up github token:
```
export GITHUB_TOKEN=xxx
```

Then build and run the utility:
```
./gradlew build
java -jar build/libs/readme-badge-replacer-0.1-all.jar
```

This will clone all repositories in the `micronaut-projects` organisation (to the current directory), and if it has a `README.md` containing the text `Revved up by Gradle Enterprise`, it will be replaced with the new text, committed and a PR will be created. 