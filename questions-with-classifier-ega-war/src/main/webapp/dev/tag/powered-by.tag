/* Copyright IBM Corp. 2015 Licensed under the Apache License, Version 2.0 */

<powered-by>
    <div id="poweredByFirstLine"></div>
    <div id="poweredBySecondLine"></div>
    
    <script>
    
    var self = this;
    
    self.on("mount", function() {
        self.poweredByFirstLine.innerHTML  = polyglot.t("poweredByFirstLine");
        self.poweredBySecondLine.innerHTML = polyglot.t("poweredBySecondLine");
        
        self.update();
    });
    
    </script>
</powered-by>
