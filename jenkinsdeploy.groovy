def findModules() {
    // Find relevant AMIs based on their name
    def sout = new StringBuffer(), serr = new StringBuffer()
    def proc = 'ls -d terraform-aws-*'.execute()
    proc.consumeProcessOutput(sout, serr)
    proc.waitForOrKill(10000)
    return sout.tokenize() 
}

def MODULE_LIST = findModules().join('\n')

pipeline {
    agent {
        docker {
            image 'hashicorp/terraform:0.12.20'
            args '--entrypoint=""'
        }
    }

    parameters {
        
        choice(name: 'module_name' , choices: MODULE_LIST, description: "module name")
    }

    stages {
         stage('Validate') {
           steps {
               sh 'echo'
           }
         }
         stage ('Lint') {
           steps {
               sh 'echo'
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