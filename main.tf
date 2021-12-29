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

  http_config = {
    http_enabled_state = "HTTP_DISABLED"
  }
  event_notification_configs {
    pubsub_topic_name = google_pubsub_topic.telemetry.id
    subfolder_matches = ""
  }

  state_notification_config = {
    pubsub_topic_name = google_pubsub_topic.devicestatus.id
  }

  log_level = "INFO"
}

resource "google_cloudiot_device" "test-device" {
  name="test-device"
  registry = google_cloudiot_registry.sensor-registry.id
  credentials {
    public_key {
      format = "RSA_PEM"
      key    = file("config/rsa_public.pem")
    }
  }
}

resource "google_cloudiot_device" "esp-test-device" {
  name="esp-test-device"
  registry = google_cloudiot_registry.sensor-registry.id
  credentials {
    public_key {
      format = "ES256_PEM"
      key    = file("config/ec_public.pem")
    }
  }
}

resource "google_storage_bucket" "function_artifacts" {
  name = "function_artifacts"
}

resource "google_storage_bucket_object" "archive" {
  name   = "process-sensor-telemtry.zip"
  bucket = "${google_storage_bucket.function_artifacts.name}"
  source = "./function/process-sensor-telemetry.zip"
}

resource "google_cloudfunctions_function" "process-sensor-telemetry" {
  name="process-sensor-telemetry"
  description ="Processes telemetry data from IoT devices"
  runtime = "go116"
  source_archive_bucket = google_storage_bucket.function_artifacts.name
  source_archive_object = google_storage_bucket_object.archive.name

  event_trigger {
    event_type = "providers/cloud.pubsub/eventTypes/topic.publish"
    resource   = google_pubsub_topic.telemetry.name
  }

  timeout = 100
  entry_point = "process_telemetry"

  available_memory_mb = 128

}
