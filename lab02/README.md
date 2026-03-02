```bash
mvn clean package dependency:copy-dependencies
java -cp "target/classes;target/dependency/*" com.dining.fluxo.App
```
