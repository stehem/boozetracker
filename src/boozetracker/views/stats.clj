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
  "
    // Create the data table.
    (function (){
      var data = new google.visualization.DataTable();
      data.addColumn('string', 'booze');
      data.addColumn('number', 'cost');
      data.addRows("
        (Stat/format-chart (Stat/pie-chart))
      ");

      // Set chart options
      var options = {'title':'Spending by drink',
      'width':400,
      'height':300};

      // Instantiate and draw our chart, passing in some options.
      var chart = new google.visualization.PieChart(document.getElementById('pie-chart'));
      chart.draw(data, options);
    })();
  "
)


(defpartial days-chart []
  "
    // Create the data table.
    (function (){
      var data = new google.visualization.DataTable();
      data.addColumn('string', 'days');
      data.addColumn('number', 'cost');
      data.addRows("
        (Stat/format-chart (Stat/days-chart))
      ");

      // Set chart options
      var options = {'title':'Spending by day',
      'width':400,
      'height':300};

      // Instantiate and draw our chart, passing in some options.
      var chart = new google.visualization.PieChart(document.getElementById('days-chart'));
      chart.draw(data, options);
    })();
  "
)


(defpartial month-chart []
  "
    // Create the data table.
    (function (){
      var data = new google.visualization.DataTable();
      data.addColumn('string', 'month');
      data.addColumn('number', 'cost');
      data.addRows("
        (Stat/format-chart (Stat/sorted-spend-month))
      ");

      // Set chart options
      var options = {'title':'Spending by month',
      'width':400,
      'height':300};

      // Instantiate and draw our chart, passing in some options.
      var chart = new google.visualization.LineChart(document.getElementById('month-chart'));
      chart.draw(data, options);
    })();
  "
)


(defpartial google-charts-setup []
    "
     // Load the Visualization API and the piechart package.
      google.load('visualization', '1.0', {'packages':['corechart']});

      // Set a callback to run when the Google Visualization API is loaded.
      google.setOnLoadCallback(drawChart);

      // Callback that creates and populates a data table,
      // instantiates the pie chart, passes in the data and
      // draws it.
      function drawChart() {
      "
        (pie-chart)
        (days-chart)
        (month-chart)
      "
      }
    "
)



(defpage-w-auth "/stats" []
  (html
    (common/layout-w-auth 
      (do (println (Stat/sorted-spend-month))
      [:div#stats
        [:script (google-charts-setup)]
        [:div#pie-chart.chart]
        [:div#days-chart.chart]
        [:div#month-chart.chart]
      ]
      )
)))

