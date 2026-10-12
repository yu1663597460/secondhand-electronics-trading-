@echo off
chcp 65001 >nul
echo.
echo ==========================================
echo    二手电子产品交易系统 - 一键启动
echo ==========================================
echo.

echo 🔍 检查服务状态...
echo.

REM 检查端口占用
netstat -an | findstr ":5173" >nul
if %errorlevel%==0 (
    echo ⚠️  警告: 端口5173已被占用，前端服务可能已在运行
    echo.
)

netstat -an | findstr ":8080" >nul
if %errorlevel%==0 (
    echo ⚠️  警告: 端口8080已被占用，后端服务可能已在运行
    echo.
)

echo ==========================================
echo    启动服务
echo ==========================================
echo.

echo 📝 提示: 将打开两个新窗口
echo    - 窗口1: 前端服务 (Vite)
echo    - 窗口2: 后端服务 (Spring Boot)
echo.

echo 🚀 正在启动前端服务...
start "前端服务 - Vite" cmd /k "cd frontend && npm run dev"

echo.
echo ⏳ 等待3秒后启动后端服务...
timeout /t 3 /nobreak >nul

echo 🚀 正在启动后端服务...
start "后端服务 - Spring Boot" cmd /k "cd backend && java -jar target/electronics-trading-1.0.0.jar"

echo.
echo ==========================================
echo    启动完成
echo ==========================================
echo.

echo ✅ 服务启动命令已执行
echo.
echo 📋 服务信息:
echo    前端地址: http://localhost:5173
echo    后端地址: http://localhost:8080
echo    API接口: http://localhost:8080/api
echo.

echo 💡 使用说明:
echo    1. 等待约10-30秒让服务完全启动
echo    2. 前端窗口显示 "Local: http://localhost:5173" 表示启动成功
echo    3. 后端窗口显示 "Started ElectronicsTradingApplication" 表示启动成功
echo    4. 在浏览器访问 http://localhost:5173 开始使用
echo.

echo 🔐 测试账号:
echo    管理员: admin / admin123
echo    用户: testuser / user123456
echo.

echo 📖 详细文档: 系统使用指南-完整版.md
echo.

echo ⚠️  注意: 请不要关闭启动的两个服务窗口
echo.

pause