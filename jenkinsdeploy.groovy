pipeline {
    agent {
        docker {
            image 'hashicorp/terraform:0.12.20'
            args '--entrypoint=""'
        }
    }

    parameters {
        choice name: 'module_name' , choices: ['key-pair', 'lambda', 'route53'], description: ''
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
                     env.module_name = 'terraform-aws-' + params.module_name
                     env.provider = 'aws'
                     env.version = params.version
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
               sh "tar -cvzf ${env.module_name}.${env.version}.tgz ${env.module_name}"
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
                            "pattern": "./${env.module_name}.${env.version}.tgz",
                            "target": "mt5225/${env.module_name}/${env.provider}/${BUILD_NUMBER}/"
                            }
                        ]
                        }"""
                )
           }         
         }
         stage('Update registry') {
             steps {
                 sh 'echo'
             }
         }
    }
}