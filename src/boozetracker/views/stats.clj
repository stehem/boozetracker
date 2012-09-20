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
    (function (){
      var data = new google.visualization.DataTable();
      data.addColumn('string', 'booze');
      data.addColumn('number', 'cost');
      data.addRows("
        (Stat/format-chart (Stat/pie-chart))
      ");
      var options = {'title':'Spending by drink',
      'width':400,
      'height':300};
      var chart = new google.visualization.PieChart(document.getElementById('pie-chart'));
      chart.draw(data, options);
    })();
  "
)


(defpartial days-chart []
  "
    (function (){
      var data = new google.visualization.DataTable();
      data.addColumn('string', 'days');
      data.addColumn('number', 'cost');
      data.addRows("
        (Stat/format-chart (Stat/days-chart))
      ");
      var options = {'title':'Spending by day',
      'width':400,
      'height':300};
      var chart = new google.visualization.PieChart(document.getElementById('days-chart'));
      chart.draw(data, options);
    })();
  "
)


(defpartial month-chart []
  "
    (function (){
      var data = new google.visualization.DataTable();
      data.addColumn('string', 'month');
      data.addColumn('number', 'cost');
      data.addRows("
        (Stat/format-chart (Stat/sorted-spend-month))
      ");
      var options = {'title':'Spending by month',
      'width':400,
      'height':300};
      var chart = new google.visualization.ColumnChart(document.getElementById('month-chart'));
      chart.draw(data, options);
    })();
  "
)



(defpartial day-chart []
  "
    (function (){
      var data = new google.visualization.DataTable();
      data.addColumn('string', 'month');
      data.addColumn('number', 'cost');
      data.addRows("
        (Stat/format-chart (Stat/sorted-spend-day))
      ");
      var options = {'title':'Spending by day',
      'hAxis': {'showTextEvery':3},
      'width':400,
      'height':300};
      var chart = new google.visualization.LineChart(document.getElementById('day-chart'));
      chart.draw(data, options);
    })();
  "
)


(defpartial avg-drink-price []
  "
    (function (){
      var data = new google.visualization.DataTable();
      data.addColumn('string', 'session');
      data.addColumn('number', 'average drink price');
      data.addRows("
        (Stat/format-chart (Stat/avg-drinks-price-session))
      ");
      var options = {'title':'Average Drink Price',
      'hAxis': {'showTextEvery':3},
      'width':400,
      'height':300};
      var chart = new google.visualization.LineChart(document.getElementById('avg-drink-price-chart'));
      chart.draw(data, options);
    })();
  "
)


(defpartial averages []
  (html
    [:div#averages.chart
      [:p "Total spend: " (Stat/total-spend)] 
      [:p "Average spend by month: " (Stat/avg-spend-month)] 
      [:p "Average spend by day: " (Stat/avg-spend-day)] 
      [:p "Average spend by session: " (Stat/avg-spend-session)] 
      [:p "Average drinks number: " (Stat/avg-drinks-nb)] 
      [:p "Average drinks price: " (Stat/avg-drinks-price)] 
     
    ]
    )
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
        (day-chart)
        (avg-drink-price)
      "
      }
    "
)



(defpage-w-auth "/stats" []
  (html
    (common/layout-w-auth 
      (do (println (Stat/avg-drinks-price-session))
      [:div#stats
        [:script (google-charts-setup)]
        [:div#pie-chart.chart]
        [:div#days-chart.chart]
        [:div#month-chart.chart]
        [:div#day-chart.chart]
        [:div#avg-drink-price-chart.chart]
        (averages) 
    
      ]
      )
)))

