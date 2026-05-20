pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'baicx/teedy'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/main']],
                    userRemoteConfigs: [[
                        url: 'https://github.com/Baicx/Teedy2.git'
                    ]]
                ])
            }
        }

        stage('Maven Build') {
            steps {
                sh 'mvn -B -DskipTests clean package'
            }
        }

        stage('Building image') {
            steps {
                script {
                    docker.build("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}")
                }
            }
        }

        stage('Run Three Containers') {
            steps {
                script {
                    echo "开始部署三个 Teedy 实例..."
                    
                    // 停止并清理所有旧容器
                    sh '''
                        echo "清理旧容器..."
                        for port in 8086 8087 8088; do
                            CONTAINER_NAME="teedy-${port}"
                            docker stop ${CONTAINER_NAME} 2>/dev/null || true
                            docker rm ${CONTAINER_NAME} 2>/dev/null || true
                        done
                    '''
                    
                    // 运行三个容器
                    sh '''
                        # 容器1: 8082端口
                        docker run -d \\
                            --name teedy-8086 \\
                            -p 8086:8080 \\
                            -e "JAVA_OPTS=-Xmx512m" \\
                            --restart unless-stopped \\
                            baicx/teedy:${BUILD_NUMBER}
                        
                        # 容器2: 8083端口
                        docker run -d \\
                            --name teedy-8087 \\
                            -p 8087:8080 \\
                            -e "JAVA_OPTS=-Xmx512m" \\
                            --restart unless-stopped \\
                            baicx/teedy:${BUILD_NUMBER}
                        
                        # 容器3: 8084端口
                        docker run -d \\
                            --name teedy-8088 \\
                            -p 8088:8080 \\
                            -e "JAVA_OPTS=-Xmx512m" \\
                            --restart unless-stopped \\
                            baicx/teedy:${BUILD_NUMBER}
                        
                        echo "所有容器已启动"
                    '''
                    
                    // 健康检查
                    sh '''
                        echo "等待应用启动..."
                        sleep 15
                        
                        echo ""
                        echo "=== 容器状态 ==="
                        docker ps --filter "name=teedy-" --format "table {{.Names}}\\t{{.Status}}\\t{{.Ports}}"
                        
                        echo ""
                        echo "=== 应用健康检查 ==="
                        SERVER_IP=$(hostname -I | awk '{print $1}')
                        for port in 8082 8083 8084; do
                            echo -n "端口 ${port}: "
                            if curl -s -f -o /dev/null --max-time 5 http://localhost:${port}/; then
                                echo "✓ 健康 (访问地址: http://${SERVER_IP}:${port})"
                            else
                                echo "✗ 不健康，查看日志: docker logs teedy-${port} --tail 10"
                            fi
                        done
                    '''
                }
            }
        }
        
        stage('Final Status Check') {
            steps {
                script {
                    sh '''
                        echo "=== 最终状态检查 ==="
                        ALL_RUNNING=true
                        
                        for port in 8082 8083 8084; do
                            CONTAINER_NAME="teedy-${port}"
                            
                            if docker ps --filter "name=${CONTAINER_NAME}" --filter "status=running" | grep -q "${CONTAINER_NAME}"; then
                                echo "✓ ${CONTAINER_NAME} 运行正常"
                            else
                                echo "✗ ${CONTAINER_NAME} 未运行"
                                ALL_RUNNING=false
                            fi
                        done
                        
                        echo ""
                        if [ "$ALL_RUNNING" = "true" ]; then
                            echo "🎉 所有三个容器都在运行！"
                            SERVER_IP=$(hostname -I | awk '{print $1}')
                            echo "访问地址:"
                            echo "  - 实例1: http://${SERVER_IP}:8082"
                            echo "  - 实例2: http://${SERVER_IP}:8083"
                            echo "  - 实例3: http://${SERVER_IP}:8084"
                        else
                            echo "⚠ 部分容器可能存在问题"
                            echo "查看容器日志:"
                            for port in 8082 8083 8084; do
                                docker logs teedy-${port} --tail 5 2>/dev/null || true
                            done
                        fi
                    '''
                }
            }
        }
    }
    
    post {
        always {
            echo "清理工作区..."
            sh '''
                # 清理 Maven 构建缓存
                rm -rf target/* 2>/dev/null || true
            '''
        }
    }
}
