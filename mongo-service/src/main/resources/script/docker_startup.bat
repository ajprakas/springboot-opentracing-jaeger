# start mongo db container and find ip address of this container using --> docker inspect -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' <container_id>

# start springboot mongo container using command

docker run -p 8102:8102 springboot-mongo:local6 -it --spring.data.mongodb.database="user_db" --spring.data.mongodb.port="27017" --spring.data.mongodb.host=172.17.0.2>
