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
                        for port in 8082 8083 8084; do
                            CONTAINER_NAME="teedy-$port"
                            docker stop ${CONTAINER_NAME} 2>/dev/null || true
                            docker rm ${CONTAINER_NAME} 2>/dev/null || true
                        done
                    '''
                    
                    // 运行三个容器
                    sh '''
                        # 容器1: 8082端口
                        docker run -d \\
                            --name teedy-8082 \\
                            -p 8082:8080 \\
                            -e "JAVA_OPTS=-Xmx512m" \\
                            --restart unless-stopped \\
                            baicx/teedy:${BUILD_NUMBER}
                        
                        # 容器2: 8083端口
                        docker run -d \\
                            --name teedy-8083 \\
                            -p 8083:8080 \\
                            -e "JAVA_OPTS=-Xmx512m" \\
                            --restart unless-stopped \\
                            baicx/teedy:${BUILD_NUMBER}
                        
                        # 容器3: 8084端口
                        docker run -d \\
                            --name teedy-8084 \\
                            -p 8084:8080 \\
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
        
        stage('Health Check and Monitoring') {
            steps {
                script {
                    sh '''
                        echo "执行深度健康检查..."
                        
                        ALL_HEALTHY=true
                        for port in 8082 8083 8084; do
                            CONTAINER_NAME="teedy-${port}"
                            
                            # 检查容器是否运行
                            if ! docker ps --filter "name=${CONTAINER_NAME}" --filter "status=running" | grep -q "${CONTAINER_NAME}"; then
                                echo "✗ 容器 ${CONTAINER_NAME} 未运行"
                                ALL_HEALTHY=false
                                continue
                            fi
                            
                            # 检查应用响应
                            RESPONSE_TIME=$(timeout 5 curl -s -w "%{time_total}" -o /dev/null http://localhost:${port}/ || echo "10.000")
                            
                            if [ "$(echo "$RESPONSE_TIME > 5" | bc -l 2>/dev/null || echo 1)" = "1" ]; then
                                echo "⚠ 容器 ${CONTAINER_NAME} 响应慢: ${RESPONSE_TIME}秒"
                            else
                                echo "✓ 容器 ${CONTAINER_NAME} 响应正常: ${RESPONSE_TIME}秒"
                            fi
                            
                            # 检查日志是否有错误
                            ERROR_COUNT=$(docker logs ${CONTAINER_NAME} --tail 20 2>&1 | grep -i "error\|exception\|fail" | wc -l)
                            if [ $ERROR_COUNT -gt 0 ]; then
                                echo "⚠ 容器 ${CONTAINER_NAME} 日志中有 ${ERROR_COUNT} 个错误"
                            fi
                        done
                        
                        if [ "$ALL_HEALTHY" = "true" ]; then
                            echo ""
                            echo "🎉 所有三个容器部署成功！"
                            echo "访问地址:"
                            echo "  - 实例1: http://$(hostname -I | awk '{print $1}'):8082"
                            echo "  - 实例2: http://$(hostname -I | awk '{print $1}'):8083"
                            echo "  - 实例3: http://$(hostname -I | awk '{print $1}'):8084"
                        else
                            echo ""
                            echo "⚠ 部分容器存在问题，请检查"
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
                
                # 清理 Docker 无用镜像
                docker system prune -f 2>/dev/null || true
            '''
        }
    }
}
