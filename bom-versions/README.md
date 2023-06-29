CLI utility to ensure micronaut-platform contains the latest releases

```
micronaut-platform$ git pull
micronaut-platform$ cd ../scripts/bom-versions/
bom-versions$ export GITHUB_TOKEN=xxx
bom-versions$ ./gradlew build
bom-versions$ java -jar build/libs/bom-versions-0.1-all.jar -f /Users/sdelamo/github/micronaut-projects
micronaut-aot bom version: 2.0.0-M3 github version: 2.0.0-M4
```
