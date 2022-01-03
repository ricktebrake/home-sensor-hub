package process_sensor_telemetry

import (
	"cloud.google.com/go/firestore"
	"context"
	firebase "firebase.google.com/go"
	"log"
	"time"
)

type PubSubMessage struct {
	Data       []byte            `json:"data"`
	Attributes map[string]string `json:"attributes"`
}

var client *firestore.Client

func init() {
	conf := &firebase.Config{ProjectID: "home-sensor-hub"}
	app, err := firebase.NewApp(ctx, conf)

	if err != nil {
		log.Fatalln(err)
	}

	client, err := app.Firestore(ctx)
	if err != nil {
		log.Fatalln(err)
	}
}

func ProcessTelemetry(ctx context.Context, m PubSubMessage) error {
	log.Printf("%s", m.Data)
	for key, value := range m.Attributes {
		log.Println("Key:", key, "Value:", value)
	}
	_, _, insertError := client.Collection("moisture-sensor").Add(ctx, map[string]interface{}{
		"timestamp": time.Now(),
		"value":     string(m.Data),
	})
	if insertError != nil {
		log.Fatalln(insertError)
	}
	return nil
}
