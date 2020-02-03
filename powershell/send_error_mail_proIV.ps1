$From = "support@limestone.on.ca"
$To = "triased@limestone.on.ca"
$Cc = "legrosm@limestone.on.ca"
$Subject = "Error detected in Pro IV V8 Client VM process"
$Body = "This is a warning for this error. Immediate action must be taken to avoid a system disruption."
$SMTPServer = "smtp.limestone.on.ca"
$SMTPPort = "587"
Send-MailMessage -From $From -to $To -Cc $Cc -Subject $Subject -Body $Body -SmtpServer $SMTPServer -port $SMTPPort
 -UseSsl -Credential (Get-Credential) -DeliveryNotificationOption OnSuccess