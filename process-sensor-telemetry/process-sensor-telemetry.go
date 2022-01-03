package process_sensor_telemetry

import (
	"context"
	"log"
)

type PubSubMessage struct {
	Data       []byte            `json:"data"`
	Attributes map[string]string `json:"attributes"`
}

func ProcessTelemetry(ctx context.Context, m PubSubMessage) error {
	log.Printf("%s", m.Data)
	for key, value := range m.Attributes {
		log.Println("Key:", key, "Value:", value)
	}

	//dbContext := context.Background()
	//conf := &firebase.Config{ProjectID: "home-sensor-hub"}
	//app, err := firebase.NewApp(ctx, conf)
	//
	//if (err != nil) {
	//	log.Fatalln(err)
	//}
	//
	//client, err := app.Firestore(dbContext)
	//if (err != nil) {
	//	log.Fatalln(err)
	//}
	//
	//_, _, insertError := client.Collection("moisture-sensor").Add(dbContext, map[string]interface{}{
	//	"timestamp": "",
	//	"value":     "",
	//})
	//
	//defer client.Close()

	return nil
}
