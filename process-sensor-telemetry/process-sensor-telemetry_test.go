package process_sensor_telemetry

import (
	"context"
	"testing"
)

func TestProcessTelemetry(t *testing.T) {
	got := ProcessTelemetry(context.Background(), PubSubMessage{Data: []byte("bla"), Attributes: map[string]string{}})
	if got != nil {
		t.Errorf("Wrong")
	}
}
