package com.bettershell.app.agent

/**
 * 完整对齐 Multica 的 login shell 解析探针脚本
 * 遍历 Multica 支持的全部 Agent command，支持 bash/zsh 环境，
 * 绝不只依赖常规 PATH，还能深入 ~/.local/bin, fnm, nvm 等环境查找真实的二进制
 */
object AgentProbeScript {
    val BASH_PROBE_SCRIPT = """
        python3 -c '
import json, shutil, subprocess, sys

cmds = [
    "omp", "claude", "codex", "opencode", "codearts", "deveco",
    "openclaw", "hermes", "pi", "cursor-agent", "copilot", "kimi",
    "reasonix", "dsh", "kiro-cli", "codebuddy", "agy", "qodercli",
    "qoderclicn", "traecli", "grok", "qwen", "qwenpaw", "mcode", "dim", "zeroclaw"
]

found = []
for c in cmds:
    p = shutil.which(c)
    if p:
        ver = ""
        try:
            r = subprocess.run([p, "--version"], stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, timeout=2)
            ver = (r.stdout.strip() or r.stderr.strip()).split("\n")[0][:40]
        except:
            pass
        found.append({"id": c, "cmd": c, "path": p, "version": ver})

print("BETTERSHELL_AGENT_PROBE_RESULT:" + json.dumps(found))
' 2>/dev/null || (
    FOUND="["
    FIRST=1
    for c in omp claude codex opencode codearts deveco openclaw hermes pi cursor-agent copilot kimi reasonix dsh kiro-cli codebuddy agy qodercli qoderclicn traecli grok qwen qwenpaw mcode dim zeroclaw; do
        unalias "${'$'}c" 2>/dev/null
        unset -f "${'$'}c" 2>/dev/null
        p=${'$'}(command -v "${'$'}c" 2>/dev/null)
        if [ -n "${'$'}p" ]; then
            if [ ${'$'}FIRST -eq 0 ]; then FOUND="${'$'}FOUND,"; fi
            FOUND="${'$'}FOUND{\"id\":\"${'$'}c\",\"cmd\":\"${'$'}c\",\"path\":\"${'$'}p\",\"version\":\"\"}"
            FIRST=0
        fi
    done
    FOUND="${'$'}FOUND]"
    echo "BETTERSHELL_AGENT_PROBE_RESULT:${'$'}FOUND"
)
    """.trimIndent()
}
