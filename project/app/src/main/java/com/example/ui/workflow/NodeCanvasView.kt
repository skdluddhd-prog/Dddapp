package com.example.ui.workflow

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.model.BotExecutionStatus
import com.example.model.NanobotRole
import com.example.model.NodeConnection
import com.example.model.RuleType
import com.example.model.WorkflowNode
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * 2D Visual Nanobot Swarm Canvas:
 * - Drag & move any Nanobot anywhere on screen (Home Base is fixed).
 * - Tap any Nanobot to assign Custom Role, System Prompt, Website URL, Initial Message & Next Bot Handoff.
 * - Displays real-time execution status (IDLE, MESSAGING WEB, DEEP THINKING, COMPLETED) and directional pipes.
 */
class NodeCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    val nodes = mutableListOf<WorkflowNode>()
    val connections = mutableListOf<NodeConnection>()

    var onNodeSelectedListener: ((WorkflowNode) -> Unit)? = null

    private val backgroundPaint = Paint().apply { color = Color.parseColor("#070B14") }
    private val gridDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1E293B")
        style = Paint.Style.FILL
    }
    private val pipePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#38BDF8")
        style = Paint.Style.STROKE
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
    }
    private val loopPipePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#A855F7")
        style = Paint.Style.STROKE
        strokeWidth = 5f
        strokeCap = Paint.Cap.ROUND
    }
    private val pipeGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2838BDF8")
        style = Paint.Style.STROKE
        strokeWidth = 15f
        strokeCap = Paint.Cap.ROUND
    }
    private val homePipePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#10B981")
        style = Paint.Style.STROKE
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
    }
    private val nodeBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#11192C")
        style = Paint.Style.FILL
    }
    private val activeNodeGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4500F0FF")
        style = Paint.Style.FILL
    }
    private val homeNodeBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#064E3B")
        style = Paint.Style.FILL
    }
    private val homeNodeBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#10B981")
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }
    private val nodeBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#38BDF8")
        style = Paint.Style.STROKE
        strokeWidth = 4.5f
    }
    private val deepThinkBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#A855F7")
        style = Paint.Style.STROKE
        strokeWidth = 5.5f
    }
    private val completedBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#10B981")
        style = Paint.Style.STROKE
        strokeWidth = 5.5f
    }
    private val selectedNodeBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F59E0B")
        style = Paint.Style.STROKE
        strokeWidth = 6.5f
    }
    private val nodeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 24f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val homeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#A7F3D0")
        textSize = 25f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val roleSubtextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#38BDF8")
        textSize = 18f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val statusSubtextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#94A3B8")
        textSize = 16f
        textAlign = Paint.Align.CENTER
    }
    private val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#38BDF8")
        style = Paint.Style.FILL
    }

    private val reusablePath = Path()
    private val arrowPath = Path()

    private var draggedNode: WorkflowNode? = null
    private var selectedNode: WorkflowNode? = null
    private var touchStartX = 0f
    private var touchStartY = 0f
    private var isDragging = false
    private val clickSlop = 15f

    init {
        post {
            if (nodes.isEmpty() && width > 0 && height > 0) {
                val homeNode = WorkflowNode(
                    title = "Home Base",
                    x = width * 0.50f,
                    y = height * 0.78f,
                    radius = 70f,
                    isHomeNode = true
                )

                val bot1 = WorkflowNode(
                    title = "Bot 1 (Fetcher)",
                    x = width * 0.22f,
                    y = height * 0.26f,
                    rule = RuleType.WEB_AUTO_MESSAGE,
                    nanobotRole = NanobotRole.WEB_MESSENGER,
                    customRoleName = "ওয়েব মেসেঞ্জার",
                    customRolePrompt = NanobotRole.WEB_MESSENGER.defaultSystemPrompt,
                    targetInput = "https://chatgpt.com",
                    initialMessageToSend = "একটি আধুনিক ওয়েব অ্যাপ বা টাস্কের মূল আর্কিটেকচার ও সোর্স কোড দাও।",
                    isHomeNode = false
                )

                val bot2 = WorkflowNode(
                    title = "Bot 2 (DeepThink)",
                    x = width * 0.52f,
                    y = height * 0.22f,
                    rule = RuleType.DEEP_THINK_ANALYZE,
                    nanobotRole = NanobotRole.DEEP_THINKER,
                    customRoleName = "ডিপ-থিংকার",
                    customRolePrompt = NanobotRole.DEEP_THINKER.defaultSystemPrompt,
                    targetInput = "AI Deep-Think Brain",
                    customParameter = "Step-by-Step Analysis",
                    isHomeNode = false
                )

                val bot3 = WorkflowNode(
                    title = "Bot 3 (Verifier)",
                    x = width * 0.80f,
                    y = height * 0.36f,
                    rule = RuleType.WEB_AUTO_MESSAGE,
                    nanobotRole = NanobotRole.CRITIC_VERIFIER,
                    customRoleName = "ভেরিফায়ার ও রিপ্লাইয়ার",
                    customRolePrompt = NanobotRole.CRITIC_VERIFIER.defaultSystemPrompt,
                    targetInput = "https://chatgpt.com",
                    customParameter = "Verify & Refine",
                    isHomeNode = false
                )

                bot1.nextTargetNodeId = bot2.id
                bot2.nextTargetNodeId = bot3.id
                bot3.nextTargetNodeId = homeNode.id

                nodes.add(homeNode)
                nodes.add(bot1)
                nodes.add(bot2)
                nodes.add(bot3)

                connections.add(NodeConnection(fromNodeId = bot1.id, toNodeId = bot2.id))
                connections.add(NodeConnection(fromNodeId = bot2.id, toNodeId = bot3.id))
                connections.add(NodeConnection(fromNodeId = bot3.id, toNodeId = homeNode.id))
                invalidate()
            }
        }
    }

    fun spawnNode(role: NanobotRole = NanobotRole.WEB_MESSENGER): WorkflowNode {
        val cloneIndex = nodes.count { !it.isHomeNode } + 1
        val centerX = (width / 2f).coerceAtLeast(120f) + ((Math.random() - 0.5f) * 180f).toFloat()
        val centerY = (height / 2.6f).coerceAtLeast(120f) + ((Math.random() - 0.5f) * 140f).toFloat()

        val defaultRule = when (role) {
            NanobotRole.WEB_MESSENGER -> RuleType.WEB_AUTO_MESSAGE
            NanobotRole.DEEP_THINKER, NanobotRole.CRITIC_VERIFIER -> RuleType.DEEP_THINK_ANALYZE
            NanobotRole.ACTION_COMPILER -> RuleType.EXTRACT_CODE
            NanobotRole.CUSTOM_AGENT -> RuleType.DEEP_THINK_ANALYZE
        }

        val home = nodes.find { it.isHomeNode }
        val newWorker = WorkflowNode(
            title = "Bot $cloneIndex",
            x = centerX,
            y = centerY,
            rule = defaultRule,
            nanobotRole = role,
            customRoleName = role.banglaTitle,
            customRolePrompt = role.defaultSystemPrompt,
            targetInput = if (defaultRule == RuleType.WEB_AUTO_MESSAGE) "https://chatgpt.com" else "AI Deep-Think Brain",
            isHomeNode = false,
            brainPower = 85,
            isCloudExecution = false,
            autoFlushMemory = true,
            nextTargetNodeId = home?.id.orEmpty()
        )

        val previousWorker = selectedNode?.takeIf { !it.isHomeNode }
            ?: nodes.lastOrNull { !it.isHomeNode }

        if (previousWorker != null) {
            connections.removeAll { it.fromNodeId == previousWorker.id && it.toNodeId == home?.id }
            connections.add(NodeConnection(fromNodeId = previousWorker.id, toNodeId = newWorker.id))
            previousWorker.nextTargetNodeId = newWorker.id
        }

        if (home != null) {
            connections.add(NodeConnection(fromNodeId = newWorker.id, toNodeId = home.id))
        }

        nodes.add(newWorker)
        selectedNode = newWorker
        invalidate()
        onNodeSelectedListener?.invoke(newWorker)
        return newWorker
    }

    fun deleteNode(nodeId: String) {
        val target = nodes.find { it.id == nodeId && !it.isHomeNode } ?: return
        nodes.remove(target)
        connections.removeAll { it.fromNodeId == nodeId || it.toNodeId == nodeId }
        if (selectedNode?.id == nodeId) {
            selectedNode = null
        }
        invalidate()
    }

    fun connectNodeToTarget(fromNodeId: String, toNodeId: String) {
        if (fromNodeId == toNodeId) return
        val fromNode = nodes.find { it.id == fromNodeId } ?: return
        fromNode.nextTargetNodeId = toNodeId
        connections.removeAll { it.fromNodeId == fromNodeId }
        connections.add(NodeConnection(fromNodeId = fromNodeId, toNodeId = toNodeId))
        invalidate()
    }

    fun applyBlueprint(
        newNodes: List<WorkflowNode>,
        newConnections: List<NodeConnection>
    ) {
        nodes.clear()
        connections.clear()
        val w = width.takeIf { it > 100 } ?: 720
        val h = height.takeIf { it > 100 } ?: 900
        newNodes.forEachIndexed { idx, node ->
            if (node.isHomeNode) {
                node.x = w / 2f
                node.y = h - 105f
            } else {
                val col = idx % 2
                val row = idx / 2
                node.x = if (col == 0) w * 0.27f else w * 0.73f
                node.y = (145f + row * 210f).coerceAtMost(h - 240f)
            }
            nodes.add(node)
        }
        connections.addAll(newConnections)
        selectedNode = nodes.firstOrNull { !it.isHomeNode }
        invalidate()
    }

    fun updateSelectedNodeConfig(
        title: String,
        nanobotRole: NanobotRole,
        customRoleName: String,
        customRolePrompt: String,
        initialMessageToSend: String,
        nextTargetNodeId: String,
        rule: RuleType,
        customParam: String,
        brainPower: Int,
        isCloud: Boolean,
        webhookUrl: String,
        targetInput: String,
        is24x7Loop: Boolean,
        loopIntervalMinutes: Int,
        autoFlushMemory: Boolean
    ) {
        selectedNode?.let { node ->
            if (!node.isHomeNode) {
                node.title = title.ifBlank { node.title }
                node.nanobotRole = nanobotRole
                node.customRoleName = customRoleName
                node.customRolePrompt = customRolePrompt
                node.initialMessageToSend = initialMessageToSend
                node.rule = rule
                node.customParameter = customParam
                node.brainPower = brainPower
                node.isCloudExecution = isCloud
                node.cloudWebhookUrl = webhookUrl
                node.targetInput = targetInput
                node.is24x7Loop = is24x7Loop
                node.loopIntervalMinutes = loopIntervalMinutes
                node.autoFlushMemory = autoFlushMemory
                if (nextTargetNodeId.isNotBlank()) {
                    connectNodeToTarget(node.id, nextTargetNodeId)
                }
                invalidate()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        val step = 56f
        var gx = step
        while (gx < width) {
            var gy = step
            while (gy < height) {
                canvas.drawCircle(gx, gy, 2f, gridDotPaint)
                gy += step
            }
            gx += step
        }

        for (conn in connections) {
            val from = nodes.find { it.id == conn.fromNodeId }
            val to = nodes.find { it.id == conn.toNodeId }
            if (from != null && to != null) {
                val isPipingToHome = to.isHomeNode
                val isLoopBack = !to.isHomeNode && nodes.indexOf(to) < nodes.indexOf(from)
                drawPipe(canvas, from.x, from.y, to.x, to.y, isPipingToHome, isLoopBack)
            }
        }

        for (node in nodes) {
            val isSelected = node.id == selectedNode?.id

            if (node.isHomeNode) {
                canvas.drawCircle(node.x, node.y, node.radius, homeNodeBodyPaint)
                canvas.drawCircle(node.x, node.y, node.radius, homeNodeBorderPaint)

                canvas.drawText("🏰 Home Base", node.x, node.y - 6f, homeTextPaint)
                canvas.drawText("(Master Screen)", node.x, node.y + 22f, statusSubtextPaint)
            } else {
                val isWorking = node.status == BotExecutionStatus.MESSAGING_WEB ||
                    node.status == BotExecutionStatus.DEEP_THINKING ||
                    node.status == BotExecutionStatus.WAITING_REPLY

                if (isWorking) {
                    canvas.drawCircle(node.x, node.y, node.radius + 14f, activeNodeGlowPaint)
                }

                val borderPaint = when {
                    isSelected -> selectedNodeBorderPaint
                    node.status == BotExecutionStatus.COMPLETED -> completedBorderPaint
                    node.nanobotRole == NanobotRole.DEEP_THINKER || node.nanobotRole == NanobotRole.CRITIC_VERIFIER ->
                        deepThinkBorderPaint
                    else -> nodeBorderPaint
                }

                canvas.drawCircle(node.x, node.y, node.radius, nodeBodyPaint)
                canvas.drawCircle(node.x, node.y, node.radius, borderPaint)

                canvas.drawText(node.title.take(14), node.x, node.y - 14f, nodeTextPaint)

                val roleLabel = node.customRoleName.ifBlank { node.nanobotRole.badge }.take(15)
                canvas.drawText(roleLabel, node.x, node.y + 9f, roleSubtextPaint)

                val statusTag = when (node.status) {
                    BotExecutionStatus.MESSAGING_WEB -> "🌐 Messaging..."
                    BotExecutionStatus.WAITING_REPLY -> "⏳ Waiting..."
                    BotExecutionStatus.DEEP_THINKING -> "🧠 DeepThink..."
                    BotExecutionStatus.COMPLETED -> "✅ Done"
                    BotExecutionStatus.PAUSED -> "⏸ Paused"
                    BotExecutionStatus.ERROR -> "⚠️ Retry"
                    BotExecutionStatus.IDLE -> if (node.isCloudExecution) "☁️ Cloud" else "⚡ Ready"
                }
                canvas.drawText(statusTag, node.x, node.y + 30f, statusSubtextPaint)
            }
        }
    }

    private fun drawPipe(
        canvas: Canvas,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        isToHome: Boolean,
        isLoopBack: Boolean
    ) {
        reusablePath.reset()
        reusablePath.moveTo(x1, y1)

        val midX = (x1 + x2) / 2f
        val curveOffset = if (isLoopBack) -90f else 0f
        reusablePath.cubicTo(midX, y1 + curveOffset, midX, y2 + curveOffset, x2, y2)

        canvas.drawPath(reusablePath, pipeGlowPaint)
        val activePipePaint = when {
            isToHome -> homePipePaint
            isLoopBack -> loopPipePaint
            else -> pipePaint
        }
        canvas.drawPath(reusablePath, activePipePaint)

        // Draw direction arrow at midpoint
        val angle = atan2((y2 - y1).toDouble(), (x2 - x1).toDouble()).toFloat()
        val arrowX = (x1 + x2) / 2f
        val arrowY = (y1 + y2) / 2f + (curveOffset * 0.5f)
        val size = 14f
        arrowPath.reset()
        arrowPath.moveTo(
            arrowX + size * cos(angle.toDouble()).toFloat(),
            arrowY + size * sin(angle.toDouble()).toFloat()
        )
        arrowPath.lineTo(
            arrowX + size * cos(angle - 2.5).toFloat(),
            arrowY + size * sin(angle - 2.5).toFloat()
        )
        arrowPath.lineTo(
            arrowX + size * cos(angle + 2.5).toFloat(),
            arrowY + size * sin(angle + 2.5).toFloat()
        )
        arrowPath.close()
        canvas.drawPath(arrowPath, arrowPaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX = event.x
                touchStartY = event.y
                isDragging = false
                val hitNode = findNodeAt(event.x, event.y)
                if (hitNode != null) {
                    draggedNode = hitNode
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - touchStartX
                val dy = event.y - touchStartY

                if (draggedNode != null && !draggedNode!!.isHomeNode) {
                    if (hypot(dx.toDouble(), dy.toDouble()) > clickSlop) isDragging = true
                    if (isDragging) {
                        draggedNode?.x = event.x.coerceIn(draggedNode!!.radius, width - draggedNode!!.radius)
                        draggedNode?.y = event.y.coerceIn(draggedNode!!.radius, height - draggedNode!!.radius)
                        invalidate()
                    }
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                if (draggedNode != null) {
                    if (!isDragging) {
                        selectedNode = draggedNode
                        onNodeSelectedListener?.invoke(draggedNode!!)
                        invalidate()
                    }
                    draggedNode = null
                    isDragging = false
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun findNodeAt(x: Float, y: Float): WorkflowNode? {
        return nodes.findLast { hypot((it.x - x).toDouble(), (it.y - y).toDouble()) <= it.radius + 16f }
    }
}
