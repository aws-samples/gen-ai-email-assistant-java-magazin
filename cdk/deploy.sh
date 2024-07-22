log_error() {
    echo "Error: $1" >&2
}

# Check if a parameter is provided
if [ -z "$1" ]; then
    log_error "Please specify the environment (available: [dev, prod]) you want to deploy"
    return
fi


# Get Metadata

cd ../lambda || exit
APP_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
ARTIFACT_ID=$(mvn help:evaluate -Dexpression=project.artifactId -q -DforceStdout)
JAR_FILE_NAME=$ARTIFACT_ID-$APP_VERSION.jar

# Create latest JAR
mvn package
cd target || exit
cp $JAR_FILE_NAME latest.jar
cd ../../cdk || exit

# Deploy CDK
if [ "$1" = "dev" ]; then
    npx cdk deploy JavaMagazinGenMailDev --outputs-file ./outputs/env_dev.json
elif [ "$1" = "prod" ]; then
    npx cdk deploy JavaMagazinGenMailProd --outputs-file ./outputs/env_prod.json
else
    echo "Error: Unknown environment: $1" >&2
fi