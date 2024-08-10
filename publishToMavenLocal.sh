export CIRCLE_TAG=$1
git tag $1
git push origin $1
./gradlew clean publishToMavenLocal