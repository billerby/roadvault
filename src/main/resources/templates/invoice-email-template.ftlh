<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 20px;
            background-color: #f4f4f4;
            color: #333;
        }
        .container {
            background-color: #fff;
            padding: 20px;
            margin: auto;
            max-width: 600px;
            border-radius: 8px;
            box-shadow: 0px 0px 10px 0px rgba(0,0,0,0.1);
        }
        .header {
            text-align: center;
            margin-bottom: 30px;
            padding-bottom: 10px;
            border-bottom: 1px solid #eee;
        }
        .invoice-details {
            background-color: #f9f9f9;
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 20px;
            border-left: 4px solid #3498db;
        }
        .highlight {
            font-weight: bold;
            color: #3498db;
        }
        .footer {
            text-align: center;
            margin-top: 20px;
            font-size: 0.8em;
            color: #777;
            padding-top: 10px;
            border-top: 1px solid #eee;
        }
        .note {
            background-color: #fffde7;
            padding: 10px;
            border-radius: 4px;
            margin: 15px 0;
            font-size: 0.9em;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin: 15px 0;
        }
        th, td {
            padding: 10px;
            text-align: left;
            border-bottom: 1px solid #eee;
        }
        th {
            background-color: #f5f5f5;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h2>Faktura från Apelgårdens vägsamfällighet</h2>
        </div>
        
        <div>
            <p>Hej ${recipientName!""}!</p>
            
            <p>Bifogat finner du faktura för vägavgift till Apelgårdens vägsamfällighet. Vänligen betala senast på förfallodagen.</p>
            
            <div class="invoice-details">
                <table>
                    <tr>
                        <th>Fastighetsbeteckning:</th>
                        <td>${propertyDesignation!""}</td>
                    </tr>
                    <tr>
                        <th>Fakturanummer:</th>
                        <td>${invoiceNumber!""}</td>
                    </tr>
                    <tr>
                        <th>Belopp:</th>
                        <td>${amount!""}</td>
                    </tr>
                    <tr>
                        <th>OCR-nummer:</th>
                        <td class="highlight">${ocrNumber!""}</td>
                    </tr>
                    <tr>
                        <th>Plusgiro:</th>
                        <td>${plusgiroNumber!""}</td>
                    </tr>
                    <tr>
                        <th>Förfallodatum:</th>
                        <td class="highlight">${dueDate!""}</td>
                    </tr>
                </table>
            </div>
            
            <div class="note">
                <p><strong>Viktigt:</strong> Ange OCR-nummer vid betalning.</p>
            </div>
            
            <p>Om du har frågor om fakturan, vänligen kontakta vägsamfällighetens styrelse.</p>
            
            <p>Med vänliga hälsningar,<br>
            Styrelsen<br>
            Apelgårdens vägsamfällighet</p>
        </div>
        
        <div class="footer">
            <p>Detta är ett automatiskt genererat meddelande. Svara inte på detta mail.</p>
        </div>
    </div>
</body>
</html>
