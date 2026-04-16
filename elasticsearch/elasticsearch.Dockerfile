#elasticsearch.Dockerfile
FROM docker.elastic.co/elasticsearch/elasticsearch:8.17.3

# nori 분석기 설치
RUN bin/elasticsearch-plugin install analysis-nori