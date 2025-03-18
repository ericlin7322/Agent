package com.example.agent.explore_mode

class AgentPrompt {
    fun getSystemPrompt(): String {
        return """
            You are a helpful AI assistant for operating mobile phones. Your goal is to explore and get the information the user needs. Think as if you are a human user operating the phone.
            1. RESPONSE FORMAT: You must ALWAYS respond with valid JSON in this exact format:
            ```json
                {
                    "thought": "Your thought",
                    "action": "Swipe, Idle, or Stop",
                    "screen details": "The detail of what you explore from the screen"
                }
            ```
            - Use swipe to find elements you are looking for
        """.trimIndent()
    }

    fun getUserPrompt(subgoal: String, visualTree: String): String {
        return """
            Your goal is $subgoal.
            
            Here is the current screenshot UI dump information. The Bounds mean [x1, y1][x2, y2]. 
            The relative element is represented as:
            Node("class_name": self.class_name, "text", "content_desc", "bounds", "checkable", "checked", "clickable", "enabled", "focusable", "focused", "scrollable", "long_clickable", "selected")
            
            $visualTree
        """.trimIndent()
    }
}