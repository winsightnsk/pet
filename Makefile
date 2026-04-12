net:
	docker compose -f docker-compose.prodnet.yaml up -d

pgmup:
	docker compose -f docker-compose.pgm.yaml up -d
pgmdown:
	docker compose -f docker-compose.pgm.yaml down