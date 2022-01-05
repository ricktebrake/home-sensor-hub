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
  type        = string
  description = "Google Cloud project id"
  default     = "home-sensor-hub"
}
variable "project_number" {
  type = string
  description = "Google Cloud project number"
  default = "588483809961"
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
  project     = var.project_id
  region      = var.region
  zone        = var.zone
}

provider "google-beta" {
  credentials = var.gcp-credentials
  project     = var.project_id
  region      = var.region
  zone        = var.zone
}

resource "google_container_cluster" "primary" {
  name               = "home-sensor-hub-cluster"
  location           = var.region
  enable_autopilot   = true
  initial_node_count = 1
  vertical_pod_autoscaling {
    enabled = true
  }
}

resource "google_artifact_registry_repository" "container-repository" {
  provider = google-beta

  location = "europe-west1"
  repository_id = "container-repository"
  description = "Docker images artifact repository"
  format = "DOCKER"
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
  name     = "test-device"
  registry = google_cloudiot_registry.sensor-registry.id
  credentials {
    public_key {
      format = "RSA_PEM"
      key    = file("config/rsa_public.pem")
    }
  }
}

resource "google_cloudiot_device" "esp-test-device" {
  name     = "esp-test-device"
  registry = google_cloudiot_registry.sensor-registry.id
  credentials {
    public_key {
      format = "ES256_PEM"
      key    = file("config/ec_public.pem")
    }
  }
}

resource "google_iam_workload_identity_pool" "github_identity_pool" {
  provider                  = google-beta
  project                   = var.project_id
  workload_identity_pool_id = "github2"
  disabled                  = false
}

resource "google_iam_workload_identity_pool_provider" "main" {
  provider                           = google-beta
  project                            = var.project_id
  workload_identity_pool_id          = google_iam_workload_identity_pool.github_identity_pool.workload_identity_pool_id
  workload_identity_pool_provider_id = "github-provider"
  attribute_mapping = {
    "google.subject"       = "assertion.sub"
    "attribute.actor"      = "assertion.actor"
    "attribute.repository" = "assertion.repository"
  }
  oidc {
    issuer_uri = "https://token.actions.githubusercontent.com"
  }
}

resource "google_service_account_iam_member" "wif-sa" {
  service_account_id = "projects/${var.project_id}/serviceAccounts/github@${var.project_id}.iam.gserviceaccount.com"
  role               = "roles/iam.workloadIdentityUser"
  member             = "principalSet://iam.googleapis.com/projects/${var.project_number}/locations/global/workloadIdentityPools/${google_iam_workload_identity_pool.github_identity_pool.workload_identity_pool_id}/attribute.repository/ricktebrake/${var.project_id}"
}
