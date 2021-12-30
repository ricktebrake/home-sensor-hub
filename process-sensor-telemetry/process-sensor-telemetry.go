package process_sensor_telemetry

import (
	"context"
	"log"
)

type PubSubMessage struct {
	Data []byte `json:"data"`
}

func ProcessTelemetry(ctx context.Context, m PubSubMessage) error {
	log.Printf("%s", m.Data)

	return nil
}
