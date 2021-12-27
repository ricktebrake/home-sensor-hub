terraform {
  required_providers {
    google = {
      source = "hashicorp/google"
      version = "4.3.0"
    }
  }
}

provider "google" {
  credentials = file("home-sensor-hub-credentials.json")
  project     = "home-sensor-hub"
  region      = "europe-west1"
  zone        = "europe-west1-b"
}

resource "google_container_cluster" "primary" {
  name               = "home-sensor-hub-cluster"
  location = "europe-west1"
  enable_autopilot   = true
  initial_node_count = 1
}