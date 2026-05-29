
# 查看保存的凭证
git config --global credential.helper


# 查看远程 URL（可能嵌入了 token）
git remote -v


# 1. 添加 .gitignore 的修改
git add .gitignore

# 2. 提交
git commit -m "将 build.gradle 加入忽略名单"

# 3. 尝试推送
git push -u origin main --force


# 推送 - 覆盖远程仓库
git push -u origin main --force


# 发行版
# 使用 GitHub CLI（需要先安装 gh）
gh release create v0.1.0 \
  --title "可乐IM v0.1.0" \
  --notes "首个预览版发布" \
  app/build/outputs/apk/debug/app-debug.apk