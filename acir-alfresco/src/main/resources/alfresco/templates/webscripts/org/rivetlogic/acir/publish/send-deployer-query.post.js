model.result = "no result";

try{
  	  
  	XMLHttpRequest.close();
        
    // get parameters
  	var extractId = args["extractId"]; /* TODO: throw 404 if this or UUIDs can't be found */
  	var uuids = argsM["uuid"];
    var deployers = argsM["deployer"];
    var storeRef = args["storeRef"];
    var contentTransformers = argsM["contentTransformer"];
    var fileNameTransformers = argsM["fileNameTransformer"];
    var filters = argsM["filter"];
    var dependencyResolvers = argsM["dependencyResolver"];
    var notifiers = argsM["notifier"];

    logger.log("extractId: " + extractId);
    logger.log("uuids: " + uuids);
    logger.log("deployers: " + deployers);
    logger.log("storeRef: " + storeRef);
    logger.log("contentTransformers: " + contentTransformers);
    logger.log("fileNameTransformers: " + fileNameTransformers);
    logger.log("filters: " + filters);
    logger.log("dependencyResolvers: " + dependencyResolvers);
    logger.log("notifiers: " + notifiers);
    
    var extractQuery = new ExtractQuery("/Data Dictionary/Web Scripts/deployer-extract-request-config.xml");
    extractQuery.addDeployerXML(deployers);
    extractQuery.addExtractId(extractId);
    extractQuery.addUUIDs(uuids);
    extractQuery.addStoreRef(storeRef);
    extractQuery.addFilters(filters);
    extractQuery.addFileNameTransformers(fileNameTransformers);
    extractQuery.addContentTransformers(contentTransformers);
    extractQuery.addDependencyResolvers(dependencyResolvers);
    extractQuery.addNotifiers(notifiers);

    // set the uuids
    logger.log("XML to send to ACIr: " + extractQuery.getXMLToSend());
	
        
}catch(ex){
	error = String(ex);
	logger.log(error);
}

try {
	// this is output in the ftl - set for extensibility
        model.result = extractQuery.getXMLToSend();
        logger.log(extractQuery.getXMLToSend());
        
        // post the result to mule
        XMLHttpRequest.open("POST", extractQuery.getACIrUrl(), false, null, null);
        XMLHttpRequest.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        //XMLHttpRequest.send(escape(extractQuery.getXMLToSend()));
        XMLHttpRequest.send("query=" + escape(extractQuery.getXMLToSend()));		
        
        var responseCode = XMLHttpRequest.getResponseCode();		
        logger.log("Response Code: "  + responseCode);
        if (responseCode != 200) {
        	XMLHttpRequest.close();
        	throw "Bad response returned from ACIr: " + responseStatus;
        }
        
        XMLHttpRequest.close();
} catch(ex) {
	error = String(ex);
	logger.log("Error trying to publish to ACIr:" + error);
	XMLHttpRequest.close();
	throw "Error trying to publish to ACIr";
}
 

function ExtractQuery(queryTemplateLocation) {


	this.addDeployerXML = function(deployers) {
		var deployerTemplate;
		var deployersXML = "";
		var deployerDefault;
		
		if (deployers != null && deployers.length > 0) {
			logger.log("Configuring " + deployers.length + " deployers");
			deployerTemplate = this.xmlTemplate.C::["deployer-template"].C::["deployer"].toXMLString();
		
			for (var i=0; i<deployers.length; i++) {
				logger.log("Adding deployer: " + deployers[i] + " to ACIr request.");
				deployersXML += deployerTemplate.replace("#{deployer}", deployers[i]);
			}
		} else {
			// set default
			deployersXML = xml.C::["deployer-default"].C::["deployer"].toXMLString();
			
		}
		this.outputXML = this.outputXML.replace("#{deployer-list}", deployersXML);
		
	}
	
	// extractId
	this.addExtractId = function(extractId) {
		logger.log("Adding " + extractId + " Extract ID.");

		this.outputXML = this.outputXML.replace("#{extract-id}", extractId);
	}
	
	// uuids
	this.addUUIDs = function(uuids) {
		logger.log("Adding " + uuids.length + " UUIDs.");
		var queryTemplate = this.xmlTemplate.C::["query-template"].C::["query"].toXMLString();
		var queryXML = "";
		for (var i=0; i<uuids.length; i++) {
			logger.log("Adding UUID: " + uuids[i] + " to ACIr request.");
			queryXML += queryTemplate.replace("#{uuid}", uuids[i]);
		}
	
		this.outputXML = this.outputXML.replace("#{query-list}", queryXML);
	}
	
	// store-ref
	this.addStoreRef = function(storeRef) {
		var storeRefXML = "";
		if (storeRef != null && storeRef != undefined) {
			logger.log("Setting storeRef as: " + storeRef);
			var storeRefTemplate = this.xmlTemplate.C::["store-ref-template"].C::["store-ref"].toXMLString();
			storeRefXML = storeRefTemplate.replace("#{store-ref}", storeRef);
		} else {
			logger.log("Using default store ref");
			storeRefXML = this.xmlTemplate.C::["store-ref-default"].C::["store-ref"].toXMLString();
		}
	
		this.outputXML = this.outputXML.replace("#{store-ref-node}", storeRefXML);
		
	}
	
	// content transformers
	this.addContentTransformers = function(transformers) {
		
		var transformerTemplate;
		var transformersXML = "";
		if (transformers != null && transformers.length > 0) {
			logger.log("Adding " + transformers.length + " content transformers.");
			transformerTemplate = this.xmlTemplate.C::["content-transformer-template"].C::["transformer"].toXMLString();
		
			for (var i=0; i<transformers.length; i++) {
				logger.log("Adding content transformer: " + transformers[i] + " to ACIr request.");
				transformersXML += transformerTemplate.replace("#{transformer}", transformers[i]);
			}
		} else {
			// set default
			transformersXML = this.xmlTemplate.C::["content-transformer-default"].C::["transformer"].toXMLString();
			
		}
		this.outputXML = this.outputXML.replace("#{content-transformers-list}", transformersXML);
	}
	
	// file name transformers
	this.addFileNameTransformers = function(transformers) {
		var transformerTemplate;
		var transformersXML = "";
		if (transformers != null && transformers.length > 0) {
			logger.log("Adding " + transformers.length + " file name transformers.");
			transformerTemplate = this.xmlTemplate.C::["file-name-transformers-template"].C::["transformer"].toXMLString();
		
			for (var i=0; i<transformers.length; i++) {
				logger.log("Adding file name transformer: " + transformers[i] + " to ACIr request.");
				transformersXML += transformerTemplate.replace("#{transformer}", transformers[i]);
			}
		} else {
			// set default
			transformersXML = this.xmlTemplate.C::["file-name-transformers-default"].C::["transformer"].toXMLString();
			
		}
		this.outputXML = this.outputXML.replace("#{file-name-transformers-list}", transformersXML);
	}
	
	// filters
	this.addFilters = function(filters) {
		var filterTemplate;
		var filtersXML = "";
		var filterDefault;
		
		if (filters != null && filters.length > 0) {
			logger.log("Configuring " + filters.length + " filters");
			filterTemplate = this.xmlTemplate.C::["filter-template"].C::["filter"].toXMLString();
		
			for (var i=0; i<filters.length; i++) {
				logger.log("Adding filter: " + filters[i] + " to ACIr request.");
				filtersXML += filterTemplate.replace("#{filter}", filters[i]);
			}
		} else {
			// set default if exist, else blank
			filtersXML = this.xmlTemplate.C::["filter-default"].C::["filter"].toXMLString();
			
		}
		this.outputXML = this.outputXML.replace("#{filter-list}", filtersXML);	
	}
	
	// dependency resolvers
	this.addDependencyResolvers = function(dependencyResolvers) {
		var depResolverTemplate;
		var depResolversXML = "";
		var depResolverDefault;
		
		if (dependencyResolvers != null && dependencyResolvers.length > 0) {
			logger.log("Configuring " + dependencyResolvers.length + " dependency resolvers");
			depResolverTemplate = this.xmlTemplate.C::["dependency-resolver-template"].C::["dependency-resolver"].toXMLString();
			logger.log("depResolverTemplate: " + depResolverTemplate);
		
			for (var i=0; i<dependencyResolvers.length; i++) {
				logger.log("Adding dependency resolver: " + dependencyResolvers[i] + " to ACIr request.");
				depResolversXML += depResolverTemplate.replace("#{dependency-resolver}", dependencyResolvers[i]);
				logger.log("depResolversXML: " + depResolversXML);
			}
		} else {
			// set default if exist, else blank
			depResolversXML = this.xmlTemplate.C::["dependency-resolver-default"].C::["dependency-resolver"].toXMLString();
			
		}
		this.outputXML = this.outputXML.replace("#{dependency-resolvers-list}", depResolversXML);	
	}
	
	// notifiers
	this.addNotifiers = function(notifiers) {
		var notifierTemplate;
		var notifiersXML = "";
		var notifierDefault;
		
		if (notifiers != null && notifiers.length > 0) {
			logger.log("Configuring " + notifiers.length + " notifiers");
			notifierTemplate = this.xmlTemplate.C::["notifier-template"].C::["notifier"].toXMLString();
		
			for (var i=0; i<notifiers.length; i++) {
				logger.log("Adding notifier: " + notifiers[i] + " to ACIr request.");
				notifiersXML += notifierTemplate.replace("#{notifier}", notifiers[i]);
			}
		} else {
			// set default if exist, else blank
			notifiersXML = this.xmlTemplate.C::["notifier-default"].C::["notifier"].toXMLString();
			
		}
		this.outputXML = this.outputXML.replace("#{notifier-list}", notifiersXML);	
	}
	
	this.getXMLToSend = function() {
		return this.outputXML;
	}
	
	this.getACIrUrl = function() {
		logger.log("ACIr url set to: " + this.xmlTemplate.C::["acir-url"] +".");
		return this.xmlTemplate.C::["acir-url"];
	}
	
	/* this is needed to be able to use hyphens in the paths to the XML nodes */
	var C = new Namespace("http://acir.rivetlogic.org/QuerySchema")
	default xml namespace = C;
	
	this.xmlTemplate = new XML(companyhome.childByNamePath(queryTemplateLocation).content);
	this.outputXML = this.xmlTemplate.C::["extract-requests"].toXMLString();
	
	
}

