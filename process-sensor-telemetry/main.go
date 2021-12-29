package main

import(
	"context"
	"log"
)

type PubSubMessage struct {
	Data []byte `json:"data"`
}

func process_telemetry(ctx context.Context, m PubSubMessage) error {
	log.Printf("%s", m.Data)

	return nil
}
