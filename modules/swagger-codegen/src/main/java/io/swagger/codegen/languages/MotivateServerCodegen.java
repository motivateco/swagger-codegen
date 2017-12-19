package io.swagger.codegen.languages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import io.swagger.codegen.CliOption;
import io.swagger.codegen.CodegenModel;
import io.swagger.codegen.CodegenModelFactory;
import io.swagger.codegen.CodegenModelType;
import io.swagger.codegen.CodegenProperty;
import io.swagger.models.ArrayModel;
import io.swagger.models.ComposedModel;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.RefModel;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.DecimalProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.PropertyBuilder;
import io.swagger.util.Json;

public class MotivateServerCodegen extends JavaJAXRSSpecServerCodegen {

	private Map<String, CodegenModel> enumModels = new HashMap<>();

	private String apiNameSuffix = "Api";

	public MotivateServerCodegen() {
		super();

		cliOptions.add( new CliOption( "apiNameSuffix", "Suffix applied to the api name" ) );

		typeMapping.put("number", "Long");
	}

	@Override
	public String getName() {
		return "jaxrs-spec-motivate";
	}


	@Override
	public void processOpts() {
		super.processOpts();

		if ( additionalProperties.containsKey("apiNameSuffix") ) {
			apiNameSuffix = additionalProperties.get( "apiNameSuffix" ).toString();
		}
	}

	@Override
	public String toApiName(final String name) {
		String computed = name;
		if ( computed.length() == 0 ) {
			return "DefaultApi";
		}
		computed = sanitizeName(computed);
		return camelize(computed) + apiNameSuffix;
	}

	@Override
	public Map<String, Object> postProcessModelsEnum(Map<String, Object> objs) {
		List<Object> models = (List<Object>) objs.get("models");
		for (Object _mo : models) {
			Map<String, Object> mo = (Map<String, Object>) _mo;
			CodegenModel cm = (CodegenModel) mo.get("model");

			// for enum model
			if (Boolean.TRUE.equals(cm.isEnum) && cm.allowableValues != null) {
				Map<String, Object> allowableValues = cm.allowableValues;
				List<Object> values = (List<Object>) allowableValues.get("values");
				List<Map<String, String>> enumVars = new ArrayList<Map<String, String>>();
				for (Object value : values) {
					Map<String, String> enumVar = new HashMap<String, String>();
					String enumName = value.toString();
					enumVar.put("name", toEnumVarName(enumName, cm.dataType));
					enumVar.put("value", toEnumValue(value.toString(), cm.dataType));
					enumVars.add(enumVar);
				}
				cm.allowableValues.put("enumVars", enumVars);
			}

			// update codegen property enum with proper naming convention
			// and handling of numbers, special characters
			for (CodegenProperty var : cm.vars) {
				updateCodegenPropertyEnum(var);
			}

		}
		return objs;
	}

	@Override
	public CodegenModel fromModel(String name, Model model, Map<String, Model> allDefinitions) {
		CodegenModel codegenModel = super.fromModel(name, model, allDefinitions);

		if ( codegenModel.isEnum ) {
			codegenModel.classname = name;
			codegenModel.classFilename = name;
			enumModels.put( name, codegenModel );
		}

		return codegenModel;
	}

	@Override
	public String toModelName(String name) {
		if ( !enumModels.containsKey( name ) ) {
			return super.toModelName( name );
		} else {
			return enumModels.get( name ).classname;
		}
	}

	@Override
	public CodegenProperty fromProperty(String name, Property p) {
		CodegenProperty property = super.fromProperty(name, p);
		property.upperCaseName = sanitizeName( property.name.toUpperCase() );
		return property;
	}

}
