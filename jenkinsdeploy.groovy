@Library('github.com/releaseworks/jenkinslib') _

def ARTIFACTORY_BASE_URL = 'http://artifactory.local:8081/artifactory'
def AWS_DEFAULT_REGION = 'us-west-2'

pipeline {
    agent {
        docker {
            image 'releaseworks/awscli:latest'
            args '--entrypoint=""'
        }
    }

    parameters {
        choice name: 'folder' , choices: ['terraform-aws-key-pair', 'terraform-aws-lambda', 'terraform-aws-route53'], description: ''
    }

    options {
        timeout(60)
        timestamps()
        ansiColor('xterm')
        disableConcurrentBuilds()
    }

    environment {
        AWS_DEFAULT_REGION = 'us-west-2'
    }

    stages {
         stage ('Create variables') {
             steps {
                 script {      
                     def json_data = readJSON file: "./${params.folder}/tf-module.json"
                     env.module_folder = params.folder            
                     env.module_name = json_data['name']
                     env.provider = json_data['provider']
                     env.namespace = json_data['namespace']
                     env.version = json_data['version']
                     env.url = "${ARTIFACTORY_BASE_URL}/${env.namespace}/${env.module_name}/${env.provider}/${BUILD_NUMBER}/${env.version}.tgz"
                     def jobdesc = sprintf("%s %s",  env.module_name, env.version)
                     currentBuild.description = jobdesc.toLowerCase()
                 }
             }
         }

         stage('Create .tgz') {
             steps {
               sh "tar -cvzf ${env.version}.tgz ${env.module_folder}"
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
                            "target": "${env.namespace}/${env.module_name}/${env.provider}/${BUILD_NUMBER}/"
                            }
                        ]
                        }"""
                )
           }         
         }
         stage('Update registry') {
             steps {
                 sh "echo ${env.url}"
                 withCredentials([[$class: 'AmazonWebServicesCredentialsBinding',  credentialsId: "aws"]]) {
                     AWS("s3 ls")
                    }
                }
            }
    }
    
}