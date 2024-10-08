console.log("This is script file")

const toggleSidebar=()=>{
	
	if($(".sidebar").is(":visible"))
	{
		$(".sidebar").css("display","none");
		$(".content").css("margin-left","0%");
	}
	else{
		$(".sidebar").css("display","block");
	    $(".content").css("margin-left","20%");
	}
};


const search=()=>{
	console.log("searching...");
	
	let query=$("#search-input").val();
	console.log(query);
	
	if(query=="")
    {
		$(".search-result").hide();
			
    }else{
		console.log(query);
		
		
		let url = `http://localhost:8282/search/${query}`;
		
		fetch(url).then((response)=>{
			return response.json();
		})
		.then((data) => {
			console.log(data);
			
			let text =`<div class='list-group'>`;
			
			data.forEach((contact) => {
				text+=`<a href='/user/show-contact/${contact.cid}' class='list-group-item list-group-item-action'>${contact.name} ( ${contact.phone} )</a>`;
			});
			
			text+=`</div>`;
			
			$(".search-result").html(text);
			$(".search-result").show();
		});
		
		
	}
};
//first request to server to create order
const paymentStart=()=>{
	
	console.log("payment started");
	
	let amount=$("#payment_field").val();
	
	console.log(amount);
	
	if(amount==''|| amount==null)
	{
		alert("amount is required !!");
		return;
	}	
	
	//code..
	//We will use Ajax to send request to server to create order
	$.ajax(
		{
			url:'/user/create_order',
			data: JSON.stringify({amount:amount,info:'order_request'}),
			contentType: 'application/json',
			type: 'POST',
			dataType: 'json',
			success:function(response){
				
				if(response.status == "created")
				{
					//open payment form
					
					let options={
						key:"rzp_test_aPLWoiumf2sP6J",
						amount:response.amount,
						currency:"INR",
						name:"Smart Contact Manager",
						description: "Donation",
						image: "https://illustoon.com/photo/7817.png",
						order_id:response.id,
						handler:function(response){
							console.log(response.razorpay_payment_id);
							console.log(response.razorpay_order_id);
							console.log(response.razorpay_signature);
							console.log('Payment successful !!');
							alert("Congrats Payment successful");
						},
						
						prefill : {
						 name: "",
						 email: "",
						 contact: ""
						 },
						 notes: {
						 address: "Smart Contact Manager"
						},
						theme: {
						 color: "#3399cc"
						 },

					};
					
					let rzp = new Razorpay(options);
					
					rzp.on('payment.failed', function (response){
					 console.log(response.error.code);
					 console.log(response.error.description);
					 console.log(response.error.source);
					 console.log(response.error.step);
					 console.log(response.error.reason);
					 console.log(response.error.metadata.order_id);
					 console.log(response.error.metadata.payment_id);
					 alert("oops payment failed");
				});
				  rzp.open();
			   }
				
			},
			error:function(error)
			{
				//invoked when error
				console.log(error);
				alert("Something went wrong !!");
			}
			
		}
		
	)
	
}