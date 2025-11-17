package community.flock.compilerplugin.fir

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.extensions.DeclarationGenerationContext
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.plugin.createMemberFunction
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

private object MapperKey : GeneratedDeclarationKey()

class MapperFirExtensionRegistrar : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +::MapperFirDeclarationGenerationExtension
    }
}

class MapperFirDeclarationGenerationExtension(
    session: FirSession
) : FirDeclarationGenerationExtension(session) {

    private val annotationId = ClassId(
        FqName("community.flock.compilerplugin"),
        Name.identifier("Mapper")
    )

    override fun getCallableNamesForClass(
        classSymbol: FirClassSymbol<*>,
        context: DeclarationGenerationContext.Member
    ): Set<Name> =
        if (classSymbol.hasAnnotation(annotationId, session))
            setOf(Name.identifier("mapper"))
        else
            emptySet()

    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirNamedFunctionSymbol> {
        val owner = context?.owner ?: return emptyList()
        if (!owner.hasAnnotation(annotationId, session)) return emptyList()

        val fn = createMemberFunction(
            owner = owner,
            key = MapperKey,
            name = callableId.callableName,
            returnTypeProvider = { session.builtinTypes.stringType.coneType }
        ) {
            visibility = Visibilities.Public
            modality = Modality.FINAL
        }

        return listOf(fn.symbol)
    }
}
