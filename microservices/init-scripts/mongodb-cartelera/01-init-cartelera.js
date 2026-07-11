// Cartelera DB: crear colecciones y seed data
db = db.getSiblingDB('cartelera_db');

// Crear colecciones
db.createCollection('movie');
db.createCollection('showtime');

// Seed: clasificaciones embebidas en movies
db.movie.insertMany([
    {
        titleMovie: "Ejemplo: Interestelar",
        synopsis: "Una aventura espacial",
        durationMinutes: 169,
        posterUrl: "",
        releaseDate: null,
        status: "Activo",
        director: "Christopher Nolan",
        genres: [
            { nameGenre: "Ciencia Ficción" },
            { nameGenre: "Aventura" }
        ],
        classification: {
            nameClassification: "PG-13",
            descriptionText: "Mayores de 13 años"
        },
        premiereWeek: true,
        isActive: true,
        createdAt: new Date()
    }
]);

db.showtime.insertMany([
    {
        movieId: "",  // se asocia desde la app después de crear la movie
        showDate: null,
        startTime: null,
        endTime: null,
        languageFormat: "Subtitulada",
        baseTicketPrice: NumberDecimal("15.00"),
        status: "Programada",
        roomId: 1,
        venueId: 1,
        availableSeats: 80
    }
]);
