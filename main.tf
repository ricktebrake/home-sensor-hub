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

variable "project_id" {
  type = string
  description = "Google Cloud project id"
  default="home-sensor-hub"
}


terraform {
  required_providers {
    google-beta = {
      source  = "hashicorp/google-beta"
      version = "4.3.0"
    }
  }
}

provider "google-beta" {
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
  location = "EUROPE-WEST1"
}

resource "google_cloudfunctions_function" "process-sensor-telemetry" {
  name="process-sensor-telemetry"
  description ="Processes telemetry data from IoT devices"
  runtime = "go116"
  source_archive_bucket = google_storage_bucket.function_artifacts.name
  source_archive_object = "process-telemetry.zip"

  event_trigger {
    event_type = "providers/cloud.pubsub/eventTypes/topic.publish"
    resource   = google_pubsub_topic.telemetry.name
  }

  timeout = 100
  entry_point = "process_telemetry"

  available_memory_mb = 128

}

resource "google_iam_workload_identity_pool" "github_identity_pool" {
  project                   = var.project_id
  workload_identity_pool_id = github
  display_name              = github
  description               = github
  disabled                  = false
}

resource "google_iam_workload_identity_pool_provider" "main" {
  project                            = var.project_id
  workload_identity_pool_id          = google_iam_workload_identity_pool.github_identity_pool.workload_identity_pool_id
  workload_identity_pool_provider_id = "github_provider"
  oidc {
    issuer_uri        = "https://token.actions.githubusercontent.com"
  }
}

resource "google_service_account_iam_member" "wif-sa" {
  service_account_id = "projects/home-sensor-hub/serviceAccounts/github@home-sensor-hub.iam.gserviceaccount.com"
  role               = "roles/iam.workloadIdentityUser"
  member             = "principalSet://iam.googleapis.com/${google_iam_workload_identity_pool.github_identity_pool.name}/attribute.repository/ricktebrake/home-sensor-hub"
}
