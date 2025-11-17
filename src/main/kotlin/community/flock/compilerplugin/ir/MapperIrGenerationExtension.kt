@file:OptIn(DeprecatedForRemovalCompilerApi::class, UnsafeDuringIrConstructionAPI::class)

package community.flock.compilerplugin.ir

import org.jetbrains.kotlin.DeprecatedForRemovalCompilerApi
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irConcat
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.name.FqName
import kotlin.collections.plusAssign

class MapperIrGenerationExtension : IrGenerationExtension {
    private val mapperAnnotation = FqName("community.flock.compilerplugin.Mapper")

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {

        moduleFragment.files.forEach { irFile ->
            irFile.declarations.filterIsInstance<IrClass>().forEach { irClass ->

                if (!irClass.hasAnnotation(mapperAnnotation)) return@forEach

                // Find existing declaration (from FIR or user) or create a new one
                val func = irClass.declarations.filterIsInstance<IrSimpleFunction>()
                    .firstOrNull { it.name.asString() == "mapper" && it.valueParameters.isEmpty() }
                    ?: error("Mapper function not found")


                func.body = DeclarationIrBuilder(pluginContext, func.symbol).irBlockBody {
                    val thisReceiver = func.dispatchReceiverParameter!!
                    val anyToString = pluginContext.irBuiltIns.anyClass.owner.functions.first { it.name.asString() == "toString" }.symbol
                    val toStringCall = irCall(anyToString).apply {
                        dispatchReceiver = irGet(thisReceiver)
                    }
                    val concat = irConcat().apply {
                        arguments += toStringCall
                        arguments.plusAssign(irString("MAPPER"))
                    }
                    +irReturn(concat)
                }
            }
        }
    }
}