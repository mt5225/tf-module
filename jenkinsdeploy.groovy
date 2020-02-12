def BASE_URL = 'http://artifactory.local:8081/artifactory'

pipeline {
    agent {
        docker {
            image 'hashicorp/terraform:0.12.20'
            args '--entrypoint=""'
        }
    }

    parameters {
        choice name: 'module_name' , choices: ['terraform-aws-key-pair', 'terraform-aws-lambda', 'terraform-aws-route53'], description: ''
        string(defaultValue: '0.0.1', description: '', name: 'version', trim: true)
    }

    options {
        timeout(60)
        timestamps()
        ansiColor('xterm')
        disableConcurrentBuilds()
    }

    stages {
         stage ('Create variables') {
             steps {
                 script {                   
                     env.module_name = params.module_name
                     env.provider = 'aws'
                     env.version = params.version
                     env.url = "${BASE_URL}/mt5225/${env.module_name}/${env.provider}/${BUILD_NUMBER}/${env.version}.tgz"
                 }
             }
         }

         stage ('Lint') {
             steps {
               dir("./${env.module_name}") {
                  sh 'terraform fmt -recursive'
               }
           }
         }

         stage('Create TGZ') {
             steps {
               sh "tar -cvzf ${env.version}.tgz ${env.module_name}"
           }

         }
         stage('Upload') {
             steps {
               rtUpload (
                    serverId: "artifactory-01",
                    spec:
                        """{
                        "files": [
                            {
                            "pattern": "./${env.version}.tgz",
                            "target": "mt5225/${env.module_name}/${env.provider}/${BUILD_NUMBER}/"
                            }
                        ]
                        }"""
                )
           }         
         }
         stage('Update registry') {
             steps {
                 sh "echo ${env.url}"
             }
         }
    }
}