(ns boozetracker.views.stats
  (:require [boozetracker.views.common :as common]
            [noir.response :as response]
            [noir.validation :as vali]
            [noir.session :as session]
            [boozetracker.db :as db]
            [boozetracker.models.cost :as Cost]
            [boozetracker.models.stat :as Stat]
            [boozetracker.models.user :as User])
  (:use [noir.core]
        [somnium.congomongo]
        [hiccup.form-helpers]
        [hiccup.page-helpers]
        [boozetracker.utils]
        [hiccup.core]))





(defpartial pie-chart []
  (html
    [:div#chart
    "
      <script type='text/javascript'>
      google.load('visualization', '1.0', {'packages':['corechart']});
      google.setOnLoadCallback(drawChart);

      function drawChart() {

        // Create the data table.
        var data = new google.visualization.DataTable();
        data.addColumn('string', 'Topping');
        data.addColumn('number', 'Slices');
        data.addRows("
        (Stat/format-pie-chart(Stat/pie-chart))
        ");

        // Set chart options
        var options = {'title':'Spending by booze type',
                       'width':400,
                       'height':300};

        // Instantiate and draw our chart, passing in some options.
        var chart = new google.visualization.PieChart(document.getElementById('pie'));
        chart.draw(data, options);
      }
      </script>
    "
    [:div#pie]
     ]
    
    
    
    )
            )



(defpage-w-auth "/stats" []
  (html
    (common/layout-w-auth 

      (do (prn (Stat/format-pie-chart (Stat/pie-chart)))
        (pie-chart)))))

