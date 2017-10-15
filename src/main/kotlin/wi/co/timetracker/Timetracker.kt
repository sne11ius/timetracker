package wi.co.timetracker

import org.springframework.context.support.ClassPathXmlApplicationContext
import tornadofx.App
import tornadofx.DIContainer
import tornadofx.FX
import tornadofx.launch
import wi.co.timetracker.view.MainView
import kotlin.reflect.KClass


class Timetracker : App(MainView::class) {
    init {
        val springContext = ClassPathXmlApplicationContext("beans.xml")
        FX.dicontainer = object : DIContainer {
            override fun <T : Any> getInstance(type: KClass<T>): T = springContext.getBean(type.java)
        }
    }
}

fun main(args: Array<String>) = launch<Timetracker>(args)
