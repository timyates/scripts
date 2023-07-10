int micronautMajorVersion = 1
new File("/Users/sdelamo/github/micronaut-projects/micronaut-docs-mn${micronautMajorVersion}").eachFile { f -> 
    if (!(f.name.startsWith(".") || f.name.contains("RC") || f.name.contains("M"))) {
        String version = f.name
        File versionFolder = new File("/Users/sdelamo/github/micronaut-projects/micronaut-docs/${version}")
        if (!versionFolder.exists()) {
            versionFolder.mkdir()
        }     
        File guideFolder = new File("/Users/sdelamo/github/micronaut-projects/micronaut-docs/${version}/guide")
        if (!guideFolder.exists()) {
            guideFolder.mkdir()
        }
        File indexHtml = new File("/Users/sdelamo/github/micronaut-projects/micronaut-docs/${version}/guide/index.html")
        if (!indexHtml.exists()) {
            indexHtml.text = """\
<!DOCTYPE html>
<html>
<head>
<meta http-equiv=\"refresh\" content=\"0; url=https://micronaut-projects.github.io/micronaut-docs-mn${micronautMajorVersion}/${version}/guide/\">
</head>
<body>
</body>
</html>            
"""
        }
    }
}