const apiUrl = 'http://localhost:8080/displayflights';

fetch(apiUrl)
  .then(response => {
    if (!response.ok) {
      throw new Error('Network response was not ok');
    }
    return response.json();
  })
  .then(data => {
    const flights = data;
    const flightsList = document.getElementById('flights-list');

    flights.forEach(flight => {
      const flightElement = document.createElement('div');
      flightElement.classList.add('flight');
      flightElement.innerHTML = `
        <p>Flight Number: ${flight.flightNumber}</p>
        <p>Price: $${flight.price}</p>
        <p>Origin: ${flight.origin}</p>
        <p>Destination: ${flight.destination}</p>
        <p>Departure Time: ${new Date(flight.departureTime).toLocaleString()}</p>
        <p>Arrival Time: ${new Date(flight.arrivalTime).toLocaleString()}</p>
        <p>Seats Left: ${flight.seatsLeft}</p>
        <p>Description: ${flight.description}</p>
      `;

      flightsList.appendChild(flightElement);
    });
  })
  .catch(error => {
    console.error('Error fetching data:', error);
  });
