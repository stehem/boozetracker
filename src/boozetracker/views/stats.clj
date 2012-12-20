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
      'backgroundColor': '#ffffff',
      width: 600, 
      height: 350,
      is3D: true
      };
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
      'backgroundColor': '#ffffff',
      width: 600, 
      height: 350,
      is3D: true
      };
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
      'backgroundColor': '#ffffff',
      width: 800, 
      height: 350,
      is3D: true,
      'hAxis': {
        slantedText:true, 
        slantedTextAngle:45
      },
      };
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
      'backgroundColor': '#ffffff',
      'hAxis': {
        slantedText:true, 
        slantedTextAngle:45
      },
      width: 800, 
      height: 350,
      is3D: true
      };
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
      'backgroundColor': '#ffffff',
      'hAxis': {'showTextEvery':3},
      width: 800, 
      height: 350,
      is3D: true,
      'hAxis': {
        slantedText:true, 
        slantedTextAngle:45
      }
      };
      var chart = new google.visualization.LineChart(document.getElementById('avg-drink-price-chart'));
      chart.draw(data, options);
    })();
  "
)


(defpartial recap-gauge []
  "
    (function (){
        var data = google.visualization.arrayToDataTable([
          ['Label', 'Value'],
          ['Alcohol', "(Stat/gauge)"]
        ]);

        var options = {
          width: 400, height: 120,
          redFrom: 90, redTo: 100,
          yellowFrom:75, yellowTo: 90,
          minorTicks: 5,
          width: 200, 
          height: 200,
          is3D: true
        };

        var chart = new google.visualization.Gauge(document.getElementById('recap-gauge'));
        chart.draw(data, options);
    })();
  "
)



(defpartial averages []
  (html
    [:div#averages.pie
     [:div#recap
      [:dl {:class "dl-horizontal"}
       [:dt "Total Spend"]
       [:dd (Stat/total-spend)] ]
     
      [:dl {:class "dl-horizontal"}
       [:dt "Average spend by month"]
       [:dd (Stat/avg-spend-month)] ]

      [:dl {:class "dl-horizontal"}
       [:dt "Average spend by day"]
       [:dd (Stat/avg-spend-day)] ]

      [:dl {:class "dl-horizontal"}
       [:dt "Average spend by session"]
       [:dd (Stat/avg-spend-session)] ]

      [:dl {:class "dl-horizontal"}
       [:dt "Average drinks number"]
       [:dd (Stat/avg-drinks-nb)] ]

      [:dl {:class "dl-horizontal"}
       [:dt "Average drinks price"]
       [:dd (Stat/avg-drinks-price)] ]
      ]
      
      [:div#gauge
        [:div#recap-gauge]
      ]
    ]
    )
)


(defpartial google-charts-setup []
    "
     // Load the Visualization API and the piechart package.
      google.load('visualization', '1.0', {'packages':['corechart', 'gauge']});

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
        (recap-gauge)
      "
      }
    "
)



(defpage-w-auth "/stats" []
  (html
    (common/layout-w-auth 
      (do 
      [:div#stats
        [:script (google-charts-setup)]
        [:div
          [:ul.tabs-links
           [:li (link-to "#averages" "Recap")]
           [:li (link-to "#pie-chart" "Spending by drink")]
           [:li (link-to "#days-chart" "Spending by day")]
           [:li (link-to "#month-chart" "Spending by month")]
           [:li (link-to "#day-chart" "Spending by date")]
           [:li (link-to "#avg-drink-price-chart" "Average drink price")]
          ]
        ]
          [:div#averages.chart (averages)]
          [:div#pie-chart.chart]  
          [:div#days-chart.chart]  
          [:div#month-chart.chart]   
          [:div#day-chart.chart] 
          [:div#avg-drink-price-chart.chart] 
      ] ) )))


