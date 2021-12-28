variable "region" {
  type        = string
  description = "Google Cloud Region"
  default     = "europe-west1"
}

variable "zone" {
  type        = string
  description = "Google Cloud Zone"
  default     = "europe-west1-b"
}

variable "gcp-credentials" {
  type        = string
  description = "Google Cloud credentials"
  default     = ""
}

variable "cloudiot-certificate" {
  type        = string
  description = "Cloud IOT Public Key Certificate"
  default     = ""
}


terraform {
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "4.3.0"
    }
  }
}

provider "google" {
  credentials = var.gcp-credentials
  project     = "home-sensor-hub"
  region      = var.region
  zone        = var.zone
}

resource "google_container_cluster" "primary" {
  name               = "home-sensor-hub-cluster"
  location           = var.region
  enable_autopilot   = true
  initial_node_count = 1
}

resource "google_pubsub_topic" "devicestatus" {
  name = "devicestatus"
}

resource "google_pubsub_topic" "telemetry" {
  name = "telemetry"
}

resource "google_cloudiot_registry" "sensor-registry" {
  name = "sensor-registry"
  mqtt_config = {
    mqtt_enabled_state = "MQTT_ENABLED"
  }
  event_notification_configs {
    pubsub_topic_name = google_pubsub_topic.telemetry.id
    subfolder_matches = ""
  }

  state_notification_config = {
    pubsub_topic_name = google_pubsub_topic.devicestatus.id
  }
}
