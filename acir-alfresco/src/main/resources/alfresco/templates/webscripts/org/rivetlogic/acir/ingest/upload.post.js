function main()
{
   try
   {
      var filename = null,
         content = null,
         mimetype = null,
         destNode = null;

      // Upload specific
      var uploadDirectoryPath = null,
       	  title = "",
          overwrite = true; 
      
      // Prevents Flash-sourced "null" values being set for those parmeters where they are invalid
      var fnFieldValue = function(p_field)
      {
         return field.value.length() > 0 && field.value !== "null" ? field.value : null;
      };

      // Parse file attributes
      for each (field in formdata.fields)
      {
         switch (String(field.name).toLowerCase())
         {
            case "filedata":
               if (field.isFile)
               {
                  filename = field.filename;
                  content = field.content;
                  mimetype = field.mimetype;
               }
               break;

            case "uploaddirectorypath":
               uploadDirectoryPath = fnFieldValue(field);
               break;

            case "filename":
               title = fnFieldValue(field);
               break;
               
            case "overwrite":
               overwrite = field.value == "true";
               break;
         }
      }

      // Ensure mandatory file attributes have been located.
      if ((filename === null || content === null) || (uploadDirectoryPath === null))
      {
         status.code = 400;
         status.message = "Required parameters are missing";
         status.redirect = true;
         return;
      }
      else
      {
         /**
          * Upload new file to destNode
          */
         if (uploadDirectoryPath !== null)
         {
            destNode = companyhome.childByNamePath(uploadDirectoryPath);
            if (destNode === null)
            {
               status.code = 404;
               status.message = "Cannot upload file since upload directory '" + uploadDirectoryPath + "' does not exist.";
               status.redirect = true;
               return;
            }
         }

         /**
          * Exitsing file handling.
          */
         var existingFile = destNode.childByNamePath(filename);
         if (existingFile !== null)
         {
            // File already exists, decide what to do
            if (overwrite)
            {
               // Upload component was configured to overwrite files if name clashes
               existingFile.properties.content.write(content);

               // Reapply mimetype as upload may have been via Flash - which always sends binary mimetype
               existingFile.properties.content.guessMimetype(filename);
               existingFile.properties.content.guessEncoding();
               existingFile.save();

               model.document = existingFile;
               // We're finished - bail out here
               return;
            }
            else
            {
               // Upload component was configured to find a new unique name for clashing filenames
               var counter = 1,
                  tmpFilename,
                  dotIndex;

               while (existingFile !== null)
               {
                  dotIndex = filename.lastIndexOf(".");
                  if (dotIndex == 0)
                  {
                     // File didn't have a proper 'name' instead it had just a suffix and started with a ".", create "1.txt"
                     tmpFilename = counter + filename;
                  }
                  else if (dotIndex > 0)
                  {
                     // Filename contained ".", create "filename-1.txt"
                     tmpFilename = filename.substring(0, dotIndex) + "-" + counter + filename.substring(dotIndex);
                  }
                  else
                  {
                     // Filename didn't contain a dot at all, create "filename-1"
                     tmpFilename = filename + "-" + counter;
                  }
                  existingFile = destNode.childByNamePath(tmpFilename);
                  counter++;
               }
               filename = tmpFilename;
            }
         }

         /**
          * Create a new file.
          */
         var newFile = destNode.createFile(filename);
         newFile.properties.content.write(content);

         // Reapply mimetype as upload may have been via Flash - which always sends binary mimetype
         newFile.properties.content.guessMimetype(filename);
         newFile.properties.content.guessEncoding();
         newFile.save();         

         // Extract metadata - via repository action for now.
         // This should use the MetadataExtracter API to fetch properties, allowing for possible failures.
         var emAction = actions.create("extract-metadata");
         if (emAction != null)
         {
            // Call using readOnly = false, newTransaction = false
            emAction.execute(newFile, false, false);
         }
         
         // Set the title if none set during meta-data extract
         newFile.reset();
         if (newFile.properties.title == null)
         {
            newFile.properties.title = title;
            newFile.save();
         }
         
         model.document = newFile;
      }
   }
   catch (e)
   {
      status.code = 500;
      status.message = "Unexpected error occured during upload of new content.";
      if (e.message && e.message.indexOf("org.alfresco.service.cmr.usage.ContentQuotaException") == 0)
      {
         status.code = 413;
         status.message = e.message;
      }
      status.redirect = true;
      return;
   }
}

main();