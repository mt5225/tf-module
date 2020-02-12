pipeline {
    agent {
        docker {
            image 'hashicorp/terraform:0.12.20'
            args '--entrypoint=""'
        }
    }

    parameters {
        choice name: 'module_name' , choices: ['key-pair', 'lambda', 'route53'], description: ''
    }

    stages {
         stage ('Create variables') {
             steps {
                 script {
                     env.module_name = 'terraform-aws-' + params.module_name
                 }
             }
         }

         stage('Validate') {
           steps {
               dir("./${env.module_name}") {
                  sh 'terraform validate'
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
               sh 'echo'
           }

         }
         stage('Upload') {
            steps {
               sh 'echo'
           }

         }
    }
}